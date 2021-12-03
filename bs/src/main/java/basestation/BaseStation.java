package basestation;

import algorithm.Algorithm;
import algorithm.AlgorithmMode;
import communication.CommunicatorBs;
import communication.CommunicatorTCP;
import communication.CommunicatorUDP;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.CommunicatorType;
import types.EventType;
import types.StateType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeMap;


public class BaseStation extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseStation.class);
    private static final String PROP_FILE_NAME = "config.properties";

    private final TreeMap<Long, Task> tasksPending = new TreeMap<>();
    private final Algorithm algorithm;
    private final CommunicatorBs communicator;

    private double q = 0;
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

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public StateType getStateX() {
        return state;
    }

    public void setState(StateType state) {
        this.state = state;
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

    public TreeMap<Long, Task> getTasksPending() {
        return tasksPending;
    }

    @Override
    public void run() {

        while (true) {
            final MessageUnpacker message = communicator.receiveMessage(50);

            try {
                final int type = message.unpackInt();
                final EventType action = EventType.getActionTypeByCode(type);
                LOGGER.debug("Received request for {}", action);

                switch (action) {
                    case TRAFFIC_ARRIVE -> {
                        final double t = message.unpackDouble();
                        final long id = message.unpackLong();
                        final double size = message.unpackDouble();
                        message.close();

                        processTrafficArrival(t, id, size);
                    }
                    case TRAFFIC_EGRESS -> {
                        final double t = message.unpackDouble();
                        message.close();

                        processTrafficEgress(t);
                    }
                    case NEW_STATE -> {
                        final int stateReceivedInt = message.unpackInt();
                        message.close();
                        final StateType stateReceived = StateType.getStateTypeByCode(stateReceivedInt);

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

            } catch (IOException e) {
                LOGGER.error("An attempt to pack / unpack a message failed. Execution completed", e);
                communicator.close();
                System.exit(-1);
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
        tasksPending.put(id, task);
        q += size;
        LOGGER.debug("Received task with ID={} SIZE={} at T={}", id, size, t);

        final double tNewState = algorithm.activationAlgorithm(false);
        final double tTrafficEgress = algorithm.processingAlgorithm();
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

        communicator.sendTrafficEgress(q, state, tTrafficEgress, tNewState, nextState, w, id, size);
    }

    public void processNewState(final StateType stateReceived) {
        LOGGER.debug("Changed to STATE={}", stateReceived);
        double tNewState = -1;

        state = stateReceived;

        switch (stateReceived) {
            case TO_ON -> {
                nextState = StateType.ON;
                tNewState = algorithm.tToOn();
            }
            case TO_OFF -> {
                nextState = StateType.OFF;
                tNewState = algorithm.tToOff();
            }
            case HYSTERESIS -> {
                nextState = StateType.TO_OFF;
                tNewState = algorithm.tHysteresis();
            }

        }

        final double tTrafficEgress = algorithm.processingAlgorithm();

        communicator.sendNewState(q, stateReceived, tTrafficEgress, tNewState, nextState);
    }

}