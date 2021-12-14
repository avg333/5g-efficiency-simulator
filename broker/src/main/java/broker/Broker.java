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

import java.io.InputStream;
import java.util.*;

public class Broker extends Thread {
    private static final String PROP_FILE_NAME = "config.properties";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RegisterServer server;
    private final RoutingAlgorithm routingAlgorithm;
    private final double tFinal;

    private final Map<Integer, Bs> mapBs = new HashMap<>();
    private final Map<Integer, Ue> mapUe = new HashMap<>();
    private final Map<Long, Event> mapEvents = new HashMap<>();
    private final LoggerCustom loggerCustom;
    private double t = 0;
    private long taskCounter = 0;

    public Broker() {
        final Properties prop = new Properties();

        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            prop.load(inputStream);
        } catch (Exception e) {
            log.error("Error loading the properties. Execution completed", e);
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
                new RegisterServerTCP(t, port, mapBs, mapUe, mapEvents) :
                new RegisterServerUDP(t, port, mapBs, mapUe, mapEvents);

        loggerCustom = new LoggerCustom(eventsLog);

        log.info("Started in [port={}] with {}, simulator time [t={}] and {}", port, routingAlgorithm, tFinal, loggerCustom);
        log.info("Press enter to start the simulation");
    }

    public static void main(String[] args) {
        new Broker().start();
    }

    private Event getNextEvent() {
        final Event event = Collections.min(mapEvents.values(), Comparator.comparing(Event::t));
        mapEvents.remove(event.id());
        return event;
    }

    private void processEvent(final Event event) {
        t = event.t();
        final EventType type = event.type();

        switch (type) {
            case TRAFFIC_INGRESS -> processTrafficIngress(event);
            case TRAFFIC_EGRESS -> processTrafficEgress(event);
            case NEW_STATE -> processNewState(event);
            default -> {
                log.error("Type {} not supported. Execution completed", type);
                System.exit(-1);
            }
        }

    }

    private void processTrafficIngress(Event event) {
        final Ue ue = (Ue) event.entity();
        try (final MessageBufferPacker requestTI = MessagePack.newDefaultBufferPacker()) {
            requestTI.packInt(EventType.TRAFFIC_INGRESS.value);

            final MessageUnpacker responseTI = ue.communicate(requestTI);

            final long taskId = taskCounter++;
            final double xUe = responseTI.unpackDouble();
            final double yUe = responseTI.unpackDouble();
            final double size = responseTI.unpackDouble();
            final double delay = responseTI.unpackDouble();
            responseTI.close();

            final long eventId = Event.getNextId();
            final Event trafficIngress = new Event(eventId, t + delay, EventType.TRAFFIC_INGRESS, ue);
            mapEvents.put(eventId, trafficIngress);

            if (size == -1)
                return;

            ue.addTask(xUe, yUe, size, delay);
            loggerCustom.logTrafficIngress(t, ue.getId(), xUe, yUe, taskId, size, delay);

            final Bs bs = processTrafficRoute(ue, taskId, size);

            processTrafficArrive(taskId, size, bs);
        } catch (Exception e) {
            log.error("An attempt to pack / unpack a Traffic Ingress message failed. Execution completed", e);
            System.exit(-1);
        }
    }

    private Bs processTrafficRoute(final Ue ue, final long taskId, final double size) {
        final Bs bs = routingAlgorithm.getBs(ue, mapBs);
        loggerCustom.logTrafficRoute(t, ue.getId(), bs.getId(), taskId, size);
        return bs;
    }

    private void processTrafficArrive(final long taskId, final double size, final Bs bs) {
        try (final MessageBufferPacker requestTA = MessagePack.newDefaultBufferPacker()) {

            requestTA.packInt(EventType.TRAFFIC_ARRIVE.value);
            requestTA.packDouble(t);
            requestTA.packLong(taskId);
            requestTA.packDouble(size);

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
                mapEvents.remove(bs.getIdEventNextState());
            } else if (state != bs.getState())
                loggerCustom.logNewState(t, bs.getId(), q, state);

            createEvents(bs, tNewState, tTrafficEgress, BsStateType.getStateTypeByCode(nextState));

            bs.addQ(q, t);
            bs.setState(state);
        } catch (Exception e) {
            log.error("An attempt to pack / unpack a Traffic Arrive message failed. Execution completed", e);
            System.exit(-1);
        }
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
            log.error("An attempt to pack / unpack a Traffic Egress message failed. Execution completed", e);
            System.exit(-1);
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
            log.error("An attempt to pack / unpack a New State message failed. Execution completed", e);
            System.exit(-1);
        }

    }

    private void createEvents(Bs bs, double tNewState, double tTrafficEgress, BsStateType nextState) {
        if (tNewState >= 0) {
            final long eventId = Event.getNextId();
            final Event newState = new Event(eventId, t + tNewState, EventType.NEW_STATE, bs);
            mapEvents.put(newState.id(), newState);
            bs.setNextState(nextState);
            bs.setIdEventNextState(newState.id());
        }

        if (tTrafficEgress > -1) {
            final long eventId = Event.getNextId();
            final Event trafficEgress = new Event(eventId, t + tTrafficEgress, EventType.TRAFFIC_EGRESS, bs);
            mapEvents.put(trafficEgress.id(), trafficEgress);
        }
    }

    @Override
    public void run() {
        server.start();
        try (final Scanner in = new Scanner(System.in)) {
            in.nextLine();
        }
        server.closeRegister();

        final long start = System.currentTimeMillis();
        while (t <= tFinal) {
            final Event event = getNextEvent();
            processEvent(event);
            loggerCustom.printProgress(t, tFinal);
        }
        final long finish = System.currentTimeMillis();

        server.closeSockets();
        loggerCustom.printResults(finish - start, t, mapBs, mapUe);
    }

}
