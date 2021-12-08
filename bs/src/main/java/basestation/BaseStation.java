package basestation;

import algorithm.Algorithm;
import algorithm.AlgorithmMode;
import communication.Communicator;
import communication.CommunicatorTCP;
import communication.CommunicatorUDP;
import communication.Message;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.BsStateType;
import types.EntityType;
import types.EventType;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


public class BaseStation extends Thread {
    private static final String PROP_FILE_NAME = "config.properties";
    private static final int MSG_LEN = 50;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Communicator communicator;
    private final Algorithm algorithm;
    private final List<Task> tasksPending = new LinkedList<>();

    private BsStateType state = BsStateType.OFF;
    private BsStateType nextState = BsStateType.OFF;
    private Task currentTask;

    public BaseStation() {
        final Properties prop = new Properties();

        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            prop.load(inputStream);
        } catch (Exception e) {
            log.error("Error loading the properties. Execution completed", e);
            System.exit(-1);
        }

        final char algorithmModeChar = prop.getProperty("algorithmMode").charAt(0);
        final AlgorithmMode mode = AlgorithmMode.getModeTypeByCode(algorithmModeChar);
        final double algorithmParam = Double.parseDouble(prop.getProperty("algorithmParam"));

        final String ipBroker = prop.getProperty("ipBroker");
        final int portBroker = Integer.parseInt(prop.getProperty("portBroker"));
        final boolean communicatorModeTCP = Boolean.parseBoolean(prop.getProperty("tcp"));
        final double x = Double.parseDouble(prop.getProperty("x"));
        final double y = Double.parseDouble(prop.getProperty("y"));
        final double c = Double.parseDouble(prop.getProperty("c"));
        final double tToOff = Double.parseDouble(prop.getProperty("tToOff"));
        final double tToOn = Double.parseDouble(prop.getProperty("tToOn"));
        final double tHysteresis = Double.parseDouble(prop.getProperty("tHysteresis"));
        algorithm = new Algorithm(this, mode, c, tToOff, tToOn, tHysteresis, algorithmParam);

        log.info("Started in position [x={} y={}] with algorithm [{}]", x, y, algorithm);

        communicator = (communicatorModeTCP) ?
                new CommunicatorTCP(EntityType.BASE_STATION, ipBroker, portBroker, x, y) :
                new CommunicatorUDP(EntityType.BASE_STATION, ipBroker, portBroker, x, y);

        log.info("Registered in {}", communicator);
    }

    public static void main(String[] args) {
        new BaseStation().start();
    }

    public BsStateType getStateX() {
        return state;
    }

    public void setNextState(BsStateType nextState) {
        this.nextState = nextState;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public List<Task> getTasksPending() {
        return tasksPending;
    }

    @Override
    public void run() {

        while (true) {
            Message request = new Message();
            try {
                request = new Message(communicator.receiveMessage(MSG_LEN));
            } catch (Exception e) {
                log.error("An attempt to unpack a message failed. Execution completed", e);
                communicator.close();
                System.exit(-1);
            }

            final EventType action = request.getAction();
            log.debug("Received request for {}", action);
            switch (action) {
                case TRAFFIC_ARRIVE -> {
                    final double t = request.getT();
                    final long id = request.getId();
                    final double size = request.getSize();
                    processTrafficArrival(t, id, size);
                }
                case TRAFFIC_EGRESS -> {
                    final double t = request.getT();
                    processTrafficEgress(t);
                }
                case NEW_STATE -> {
                    final BsStateType stateReceived = request.getStateReceived();
                    processNewState(stateReceived);
                }
                case CLOSE -> {
                    communicator.close();
                    log.info("Execution completed");
                    return;
                }
                default -> {
                    log.error("Type {} not supported. Execution completed", action);
                    communicator.close();
                    System.exit(-1);
                }
            }


        }

    }

    public void processTrafficArrival(final double t, final long id, final double size) {
        final Task task = new Task(id, size, t);
        tasksPending.add(task);
        log.debug("Received task with ID={} SIZE={} at T={}", id, size, t);

        final double tNewState = algorithm.activationAlgorithm(EventType.TRAFFIC_ARRIVE);
        final double tTrafficEgress = algorithm.processingAlgorithm();

        final double q = tasksPending.stream().mapToDouble(Task::size).sum();
        final double a = Task.getDelay(t);
        sendTrafficArrival(q, state, tTrafficEgress, tNewState, nextState, a);
    }

    public void processTrafficEgress(final double t) {
        final long id = currentTask.id();
        final double size = currentTask.size();
        final double w = t - currentTask.tArrive() - currentTask.size() / algorithm.c();
        log.debug("Processed task with ID={} SIZE={} at T={}", id, size, t);

        currentTask = null;

        final double tNewState = algorithm.suspensionAlgorithm();
        final double tTrafficEgress = algorithm.processingAlgorithm();

        final double q = tasksPending.stream().mapToDouble(Task::size).sum();
        sendTrafficEgress(q, state, tTrafficEgress, tNewState, nextState, w, id, size);
    }

    public void processNewState(final BsStateType stateReceived) {
        if (stateReceived != BsStateType.TO_OFF || state == BsStateType.HYSTERESIS) {
            log.debug("Changed to STATE={}", stateReceived);
        }

        double tNewState = -1;

        switch (stateReceived) {
            case ON, OFF -> state = stateReceived;
            case TO_ON -> {
                state = stateReceived;
                nextState = BsStateType.ON;
                tNewState = algorithm.tToOn();
            }
            case TO_OFF -> {
                if (state == BsStateType.HYSTERESIS) {
                    state = stateReceived;
                    nextState = BsStateType.OFF;
                    tNewState = algorithm.tToOff();
                }
            }
            case WAITING_TO_ON -> {
                state = stateReceived;
                nextState = BsStateType.TO_ON;
                tNewState = algorithm.algorithmParam();
            }
            case HYSTERESIS -> {
                state = stateReceived;
                nextState = BsStateType.TO_OFF;
                tNewState = algorithm.tHysteresis();
            }
        }

        final double tTrafficEgress = algorithm.processingAlgorithm();

        final double q = tasksPending.stream().mapToDouble(Task::size).sum();
        sendNewState(q, stateReceived, tTrafficEgress, tNewState, nextState);
    }

    private void sendTrafficArrival(double q, BsStateType state, double tTrafficEgress, double tNewState, BsStateType nextState, double a) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(state.value);
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(nextState.value);
            response.packDouble(a);
            communicator.sendMessage(response);
        } catch (Exception e) {
            log.error("Error packing the message Traffic Arrival. Execution completed", e);
            communicator.close();
            System.exit(-1);
        }
    }

    private void sendTrafficEgress(double q, BsStateType state, double tTrafficEgress, double tNewState, BsStateType nextState, double w,
                                   long id, double size) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(state.value);
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(nextState.value);
            response.packDouble(w);
            response.packLong(id);
            response.packDouble(size);
            communicator.sendMessage(response);
        } catch (Exception e) {
            log.error("Error packing the message Traffic Egress. Execution completed", e);
            communicator.close();
            System.exit(-1);
        }
    }

    private void sendNewState(double q, BsStateType stateReceived, double tTrafficEgress, double tNewState, BsStateType nextState) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(stateReceived.value);
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(nextState.value);
            communicator.sendMessage(response);
        } catch (Exception e) {
            log.error("Error packing the message New State. Execution completed", e);
            communicator.close();
            System.exit(-1);
        }
    }

}