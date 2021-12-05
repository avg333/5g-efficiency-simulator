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
import types.BsStateType;
import types.EventType;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Broker extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Broker.class);
    private static final String PROP_FILE_NAME = "config.properties";

    private final RegisterServer server;
    private final RoutingAlgorithm routingAlgorithm;
    private final double tFinal;

    private final Map<Integer, Bs> listaBS = new TreeMap<>();
    private final Map<Integer, Ue> listaUE = new TreeMap<>();
    private final Map<Long, Event> events = new TreeMap<>();
    private final LoggerCustom loggerCustom;
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
        final boolean eventsLog = Boolean.parseBoolean(prop.getProperty("eventsLog"));
        final char routingAlgorithmModeChar = prop.getProperty("routingAlgorithmMode").charAt(0);
        final RoutingAlgorithmMode routingAlgorithmMode = RoutingAlgorithmMode.getRoutingAlgorithmModeTypeByCode(routingAlgorithmModeChar);
        tFinal = Double.parseDouble(prop.getProperty("tFinal"));
        routingAlgorithm = new RoutingAlgorithm(routingAlgorithmMode);

        server = (communicatorModeTCP) ?
                new RegisterServerTCP(t, port, listaBS, listaUE, events) :
                new RegisterServerUDP(t, port, listaBS, listaUE, events);

        loggerCustom = new LoggerCustom(eventsLog);

        LOGGER.info("broker.Broker iniciado con los parametros:\n\tport={} csv={}", port, eventsLog);
        LOGGER.info("\nPulsa enter para iniciar la simulaci�n. ");
    }

    public static void main(String[] args) {
        new Broker().start();
    }

    private Event getNextEvent() {
        final Event event = Collections.min(events.values(), Comparator.comparing(Event::t));
        events.remove(event.id());
        return event;
    }

    private void processEvent(final Event event) {
        t = event.t();
        final EventType type = event.type();

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
        final Ue ue = (Ue) event.entity();

        final MessageBufferPacker requestTI = MessagePack.newDefaultBufferPacker();
        requestTI.packInt(EventType.TRAFFIC_INGRESS.value);
        requestTI.close();

        final MessageUnpacker responseTI = ue.communicate(requestTI);

        final long taskId = taskCounter++;
        final double xUe = responseTI.unpackDouble();
        final double yUe = responseTI.unpackDouble();
        final double size = responseTI.unpackDouble();
        final double delay = responseTI.unpackDouble();
        responseTI.close();

        final long eventId = Event.getNextId();
        final Event trafficIngress = new Event(EventType.TRAFFIC_INGRESS, eventId, t + delay, ue);
        events.put(eventId, trafficIngress);

        if (size == -1)
            return;

        ue.addTask(xUe, yUe, size, delay);
        loggerCustom.logTrafficIngress(t, ue.getId(), xUe, yUe, taskId, size, delay);

        final Bs bs = routingAlgorithm.getBs(ue, listaBS);
        loggerCustom.logTrafficRoute(t, ue.getId(), bs.getId(), taskId, size);

        final MessageBufferPacker requestTA = MessagePack.newDefaultBufferPacker();
        requestTA.packInt(EventType.TRAFFIC_ARRIVE.value);
        requestTA.packDouble(t);
        requestTA.packLong(taskId);
        requestTA.packDouble(size);
        requestTA.close();
        final MessageUnpacker responseTA = bs.communicate(requestTA);

        final double q = responseTA.unpackDouble();
        final BsStateType state = BsStateType.getStateTypeByCode(responseTA.unpackInt());
        final double tTrafficEgress = responseTA.unpackDouble();
        final double tNewState = responseTA.unpackDouble();
        final int nextState = responseTA.unpackInt();
        final double a = responseTA.unpackDouble();
        responseTA.close();

        loggerCustom.logTrafficArrival(t, bs.getId(), taskId, size, q, a);

        if (bs.getState() == BsStateType.HYSTERESIS) {
            loggerCustom.logNewState(t, bs.getId(), q, state);
            events.remove(bs.getIdEventNextState());
        } else if (state != bs.getState())
            loggerCustom.logNewState(t, bs.getId(), q, state);

        createEvents(bs, tNewState, tTrafficEgress, BsStateType.getStateTypeByCode(nextState));

        bs.addQ(q, t);
        bs.setState(state);
    }

    private void processTrafficEgress(Event event) {
        final Bs bs = (Bs) event.entity();

        try (final MessageBufferPacker requestTE = MessagePack.newDefaultBufferPacker()) {
            requestTE.packInt(EventType.TRAFFIC_EGRESS.value);
            requestTE.packDouble(t);
            final MessageUnpacker responseTE = bs.communicate(requestTE);

            final double q = responseTE.unpackDouble();
            final BsStateType state = BsStateType.getStateTypeByCode(responseTE.unpackInt());
            final double tTrafficEgress = responseTE.unpackDouble();
            final double tNewState = responseTE.unpackDouble();
            final int nextState = responseTE.unpackInt();
            final double w = responseTE.unpackDouble();
            final long id = responseTE.unpackLong();
            final double size = responseTE.unpackDouble();
            responseTE.close();

            loggerCustom.logTrafficEgress(t, bs.getId(), id, size, q, w);

            if (state != bs.getState())
                loggerCustom.logNewState(t, bs.getId(), q, state);

            createEvents(bs, tNewState, tTrafficEgress, BsStateType.getStateTypeByCode(nextState));

            bs.addQ(q, t);
            bs.addW(w);
            bs.setState(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processNewState(Event event) {
        final Bs bs = (Bs) event.entity();
        BsStateType nextState = bs.getNextStateBs();

        try (final MessageBufferPacker requestNS = MessagePack.newDefaultBufferPacker()) {
            requestNS.packInt(EventType.NEW_STATE.value);
            requestNS.packInt(nextState.value);
            final MessageUnpacker responseNS = bs.communicate(requestNS);

            final double q = responseNS.unpackDouble();
            final BsStateType state = BsStateType.getStateTypeByCode(responseNS.unpackInt());
            final double tTrafficEgress = responseNS.unpackDouble();
            final double tNewState = responseNS.unpackDouble();
            nextState = BsStateType.getStateTypeByCode(responseNS.unpackInt());
            responseNS.close();

            if (state != bs.getState())
                loggerCustom.logNewState(t, bs.getId(), q, state);

            createEvents(bs, tNewState, tTrafficEgress, nextState);

            bs.setState(state);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createEvents(Bs bs, double tNewState, double tTrafficEgress, BsStateType nextState) {
        if (tNewState >= 0) {
            final long eventId = Event.getNextId();
            final Event newState = new Event(EventType.NEW_STATE, eventId, t + tNewState, bs);
            events.put(newState.id(), newState);
            bs.setNextState(nextState);
            bs.setIdEventNextState(newState.id());
        }

        if (tTrafficEgress > -1) {
            final long eventId = Event.getNextId();
            final Event trafficEgress = new Event(EventType.TRAFFIC_EGRESS, eventId, t + tTrafficEgress, bs);
            events.put(trafficEgress.id(), trafficEgress);
        }
    }

    @Override
    public void run() {
        server.start();
        final Scanner in = new Scanner(System.in);
        in.nextLine();
        in.close();
        server.closeRegister();

        final long start = System.currentTimeMillis();
        while (t <= tFinal) {
            final Event event = getNextEvent();
            processEvent(event);
            loggerCustom.printProgress(t, tFinal);
        }
        final long finish = System.currentTimeMillis();

        server.closeSockets();
        loggerCustom.imprimirResultados(finish - start, t, listaBS, listaUE);
    }

}
