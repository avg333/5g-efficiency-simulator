package broker;

import communication.RegisterServer;
import communication.RegisterServerTCP;
import communication.RegisterServerUDP;
import entities.Bs;
import entities.Ue;
import loggers.LoggerCustom;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.RoutingAlgorithm;
import routing.RoutingAlgorithmMode;
import types.EventType;
import types.StateType;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Broker extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Broker.class);
    private static final String PROP_FILE_NAME = "config.properties";

    private final RegisterServer servidor;
    private final RoutingAlgorithm routingAlgorithm;
    private final double tFinal;

    private final Map<Integer, Bs> listaBS = new TreeMap<>();
    private final Map<Integer, Ue> listaUE = new TreeMap<>();
    private final Map<Long, Event> events = new TreeMap<>();
    private double t = 0;
    private long taskCounter = 0;

    public Broker() {
        final Properties prop = new Properties();

        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            prop.load(inputStream);
        } catch (Exception e) {
            LOGGER.error("Error loading the properties. Execution completed", e);
            System.exit(-1);
        }

        final int port = Integer.parseInt(prop.getProperty("port"));
        final boolean communicatorModeTCP = Boolean.parseBoolean(prop.getProperty("tcp"));
        final boolean verbosity = Boolean.parseBoolean(prop.getProperty("verbosity"));
        final boolean eventsLog = Boolean.parseBoolean(prop.getProperty("eventsLog"));
        final char routingAlgorithmModeChar = prop.getProperty("routingAlgorithmMode").charAt(0);
        final RoutingAlgorithmMode routingAlgorithmMode = RoutingAlgorithmMode.getRoutingAlgorithmModeTypeByCode(routingAlgorithmModeChar);
        tFinal = Double.parseDouble(prop.getProperty("tFinal"));
        routingAlgorithm = new RoutingAlgorithm(routingAlgorithmMode);

        servidor = (communicatorModeTCP) ?
                new RegisterServerTCP(t, port, listaBS, listaUE, events) :
                new RegisterServerUDP(t, port, listaBS, listaUE, events);

        LoggerCustom.setSettings(verbosity, eventsLog);

        LOGGER.info("broker.Broker iniciado con los parametros:\n\tport={} verbosity={} csv={}", port, verbosity, eventsLog);
        LOGGER.info("\nPulsa enter para iniciar la simulaci�n. ");
    }

    public static void main(String[] args) {
        new Broker().start();
    }

    private Event getNextEvent() {
        final Event event = Collections.min(events.values(), Comparator.comparing(Event::getT));
        events.remove(event.getId());
        return event;
    }

    private void processEvent(final Event event) {
        t = event.getT();
        final EventType type = event.getType();

        try {
            switch (type) {
                case TRAFFIC_INGRESS -> processTrafficIngress(event);
                case TRAFFIC_EGRESS -> processTrafficEgress(event);
                case NEW_STATE -> processNewState(event);
                default -> {
                    LOGGER.error("Type {} not supported. Execution completed", type);
                    System.exit(-1);
                }
            }
        } catch (Exception e) {
            LOGGER.error("An attempt to pack / unpack a message failed. Execution completed", e);
            System.exit(-1);
        }

    }

    private void processTrafficIngress(Event event) throws IOException {
        final Ue ue = (Ue) event.getEntity();
        final MessageBufferPacker requestTI = MessagePack.newDefaultBufferPacker();
        int eventCode = EventType.getCodeByActionType(EventType.TRAFFIC_INGRESS);
        requestTI.packInt(eventCode).close();
        MessageUnpacker responseTI = ue.communicate(requestTI);

        long idTarea = taskCounter++;
        double xUe = responseTI.unpackDouble();
        double yUe = responseTI.unpackDouble();
        double size = responseTI.unpackDouble();
        double delay = responseTI.unpackDouble();
        responseTI.close();

        final long eventId = Event.getNextId();
        Event trafficIngress = new Event(EventType.TRAFFIC_INGRESS, eventId, t + delay, ue);
        events.put(trafficIngress.getId(), trafficIngress);

        if (size == -1)
            return;

        ue.addTask(xUe, yUe, size, delay);
        LoggerCustom.logTrafficIngress(t, ue.getId(), xUe, yUe, idTarea, size, delay);

        Bs bs = routingAlgorithm.getBs(ue, listaBS);
        LoggerCustom.logTrafficRoute(t, ue.getId(), bs.getId(), idTarea, size);

        final MessageBufferPacker requestTA = MessagePack.newDefaultBufferPacker();
        eventCode = EventType.getCodeByActionType(EventType.TRAFFIC_ARRIVE);
        requestTA.packInt(eventCode).packDouble(t).packLong(idTarea).packDouble(size);
        requestTA.close();
        MessageUnpacker responseTA = bs.communicate(requestTA);

        double q = responseTA.unpackDouble();
        StateType state = StateType.getStateTypeByCode(responseTA.unpackInt());
        double tTrafficEgress = responseTA.unpackDouble();
        double tNewState = responseTA.unpackDouble();
        int nextState = responseTA.unpackInt();
        double a = responseTA.unpackDouble();
        responseTA.close();

        LoggerCustom.logTrafficArrival(t, bs.getId(), idTarea, size, q, a);

        if (bs.getState() == StateType.HYSTERESIS) {
            LoggerCustom.logNewState(t, bs.getId(), q, state);
            events.remove(bs.getIdEventNextState());
        } else if (state != bs.getState())
            LoggerCustom.logNewState(t, bs.getId(), q, state);

        createEvents(bs, tNewState, tTrafficEgress, StateType.getStateTypeByCode(nextState));

        bs.addQ(q, t);
        bs.setState(state);
    }

    private void processTrafficEgress(Event event) {
        final Bs bs = (Bs) event.getEntity();
        int eventCode = EventType.getCodeByActionType(EventType.TRAFFIC_EGRESS);

        try (final MessageBufferPacker requestTE = MessagePack.newDefaultBufferPacker()) {
            requestTE.packInt(eventCode).packDouble(t).close();
            MessageUnpacker responseTE = bs.communicate(requestTE);

            double q = responseTE.unpackDouble();
            StateType state = StateType.getStateTypeByCode(responseTE.unpackInt());
            double tTrafficEgress = responseTE.unpackDouble();
            double tNewState = responseTE.unpackDouble();
            int nextState = responseTE.unpackInt();
            double w = responseTE.unpackDouble();
            long id = responseTE.unpackLong();
            double size = responseTE.unpackDouble();
            responseTE.close();

            LoggerCustom.logTrafficEgress(t, bs.getId(), id, size, q, w);

            if (state != bs.getState())
                LoggerCustom.logNewState(t, bs.getId(), q, state);

            createEvents(bs, tNewState, tTrafficEgress, StateType.getStateTypeByCode(nextState));

            bs.addQ(q, t);
            bs.addW(w);
            bs.setState(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processNewState(Event event) {
        final Bs bs = (Bs) event.getEntity();
        StateType nextState = bs.getNextStateBs();
        final int eventCode = EventType.getCodeByActionType(EventType.NEW_STATE);
        final int eventCode2 = StateType.getCodeByStateType(nextState);

        try (final MessageBufferPacker requestNS = MessagePack.newDefaultBufferPacker()) {
            requestNS.packInt(eventCode).packInt(eventCode2).close();
            MessageUnpacker responseNS = bs.communicate(requestNS);

            double q = responseNS.unpackDouble();
            StateType state = StateType.getStateTypeByCode(responseNS.unpackInt());
            double tTrafficEgress = responseNS.unpackDouble();
            double tNewState = responseNS.unpackDouble();
            nextState = StateType.getStateTypeByCode(responseNS.unpackInt());
            responseNS.close();

            if (state != bs.getState())
                LoggerCustom.logNewState(t, bs.getId(), q, state);

            createEvents(bs, tNewState, tTrafficEgress, nextState);

            bs.setState(state);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createEvents(Bs bs, double tNewState, double tTrafficEgress, StateType nextState) {
        if (tNewState > 0) {
            final long eventId = Event.getNextId();
            final Event newState = new Event(EventType.NEW_STATE, eventId, t + tNewState, bs);
            events.put(newState.getId(), newState);
            bs.setNextState(nextState);
            bs.setIdEventNextState(newState.getId());
        }

        if (tTrafficEgress > -1) {
            final long eventId = Event.getNextId();
            final Event trafficEgress = new Event(EventType.TRAFFIC_EGRESS, eventId, t + tTrafficEgress, bs);
            events.put(trafficEgress.getId(), trafficEgress);
        }
    }

    @Override
    public void run() {
        servidor.start();
        final Scanner in = new Scanner(System.in);
        in.nextLine();
        in.close();
        servidor.closeRegister();

        final long start = System.currentTimeMillis();
        while (t <= tFinal) {
            final Event event = getNextEvent();
            processEvent(event);
            LoggerCustom.printProgress(t, tFinal);
        }
        final long finish = System.currentTimeMillis();

        servidor.closeSockets();
        LoggerCustom.imprimirResultados(finish - start, t, listaBS, listaUE);
    }

}
