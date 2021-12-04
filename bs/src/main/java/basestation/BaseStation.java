package basestation;

import algorithm.Algorithm;
import algorithm.AlgorithmMode;
import communication.CommunicatorBs;
import communication.CommunicatorTCP;
import communication.CommunicatorUDP;
import communication.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.CommunicatorType;
import types.EventType;
import types.StateType;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


public class BaseStation extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseStation.class);
    private static final String PROP_FILE_NAME = "config.properties";
    private static final int MSG_LEN = 50;

    private final List<Task> tasksPending = new LinkedList<>();
    private final Algorithm algorithm;
    private final CommunicatorBs communicator;

    private StateType state = StateType.OFF;
    private StateType nextState = StateType.OFF;
    private Task currentTask;

    public BaseStation() {
        final Properties prop = new Properties();

        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            prop.load(inputStream);
        } catch (Exception e) {
            LOGGER.error("Error loading the properties. Execution completed", e);
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

        communicator = (communicatorModeTCP) ?
                new CommunicatorBs(new CommunicatorTCP(CommunicatorType.BASE_STATION, ipBroker, portBroker, x, y)) :
                new CommunicatorBs(new CommunicatorUDP(CommunicatorType.BASE_STATION, ipBroker, portBroker, x, y));

        LOGGER.info("Started");
        LOGGER.info("communicator: {}", communicator);
        LOGGER.info("position: x={} y={}", x, y);
        LOGGER.info("algorithm: {}", algorithm);
    }

    public static void main(String[] args) {
        new BaseStation().start();
    }

    public StateType getStateX() {
        return state;
    }

    public void setNextState(StateType nextState) {
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
            Message message = new Message();
            try{
                message = new Message(communicator.receiveMessage(MSG_LEN));
            } catch (Exception e) {
                LOGGER.error("An attempt to pack / unpack a message failed. Execution completed", e);
                communicator.close();
                System.exit(-1);
            }

            final EventType action = message.getAction();
            LOGGER.debug("Received request for {}", action);
            switch (action) {
                case TRAFFIC_ARRIVE -> {
                    final double t = message.getT();
                    final long id = message.getId();
                    final double size = message.getSize();
                    processTrafficArrival(t, id, size);
                }
                case TRAFFIC_EGRESS -> {
                    final double t = message.getT();
                    processTrafficEgress(t);
                }
                case NEW_STATE -> {
                    final StateType stateReceived = message.getStateReceived();
                    processNewState(stateReceived);
                }
                case CLOSE -> {
                    communicator.close();
                    LOGGER.info("Execution completed");
                    return;
                }
                default -> {
                    LOGGER.error("Type {} not supported. Execution completed", action);
                    communicator.close();
                    System.exit(-1);
                }
            }


        }

    }

    /**
     * A la BS le llega una tarea de una UE. Esta tarea se almacena en la lista de tareas.
     * Despues de eso la BS puede:
     * <ul>
     * <li>Procesarla -> Se envia el tiempo que tardara en procesarla</li>
     * <li>Cambiar de estado -> Se envia el tiempo que tardara en cambiar de estado y el siguiente estado</li>
     * <li>No hacer ninguna -> no se envia ninguno de los dos anteriores</li>
     * </ul>
     * Siempre se envian los siguientes datos:
     * <ul>
     * <li>q: tamaño de la cola</li>
     * <li>state: estado actual</li>
     * <li>a: tiempo desde la ultima llegada hasta esta</li>
     * </ul>
     *
     * @param t    instante en el que llega la tarea
     * @param id   identificador unico de la tarea en el sistema
     * @param size tamaño de la tarea
     */
    public void processTrafficArrival(final double t, final long id, final double size) {
        final Task task = new Task(id, size, t);
        tasksPending.add(task);
        LOGGER.debug("Received task with ID={} SIZE={} at T={}", id, size, t);

        final double tNewState = algorithm.activationAlgorithm(EventType.TRAFFIC_ARRIVE);
        final double tTrafficEgress = algorithm.processingAlgorithm();

        final double q = tasksPending.stream().mapToDouble(Task::getSize).sum();
        final double a = Task.getDelay(t);
        communicator.sendTrafficArrival(q, state, tTrafficEgress, tNewState, nextState, a);
    }

    public void processTrafficEgress(final double t) {
        final long id = currentTask.getId();
        final double size = currentTask.getSize();
        final double w = t - currentTask.getArrive() - currentTask.getSize() / algorithm.c();
        LOGGER.debug("Processed task with ID={} SIZE={} at T={}", id, size, t);

        currentTask = null;

        final double tNewState = algorithm.suspensionAlgorithm();
        final double tTrafficEgress = algorithm.processingAlgorithm();

        final double q = tasksPending.stream().mapToDouble(Task::getSize).sum();
        communicator.sendTrafficEgress(q, state, tTrafficEgress, tNewState, nextState, w, id, size);
    }

    public void processNewState(final StateType stateReceived) {
        state = stateReceived;
        LOGGER.debug("Changed to STATE={}", stateReceived);

        double tNewState;

        switch (stateReceived) {
            case TO_ON -> {
                nextState = StateType.ON;
                tNewState = algorithm.tToOn();
            }
            case TO_OFF -> {
                nextState = StateType.OFF;
                tNewState = algorithm.tToOff();
            }
            case WAITING_TO_ON -> {
                nextState = StateType.TO_ON;
                tNewState = algorithm.algorithmParam();
            }
            case HYSTERESIS -> {
                nextState = StateType.TO_OFF;
                tNewState = algorithm.tHysteresis();
            }
            default -> tNewState = -1;
        }

        final double tTrafficEgress = algorithm.processingAlgorithm();

        final double q = tasksPending.stream().mapToDouble(Task::getSize).sum();
        communicator.sendNewState(q, stateReceived, tTrafficEgress, tNewState, nextState);
    }

}