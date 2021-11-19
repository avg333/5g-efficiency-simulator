package basestation;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import types.Communicator;
import types.actionType;
import types.communicatorType;
import types.stateType;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BaseStation extends Thread {

    private static final Logger logger = Logger.getLogger(BaseStation.class.getName());
    private static final String HELP_MSG = """
            Argumentos obligatorios:
            \t<ID> <X> <Y>
            Argumentos opcionales:
            \t[--host <IP> <PUERTO>] [-s|--size <DISTR> <PARAM1> <PARAM2>] [-d|--delay <DISTR> <PARAM1> <PARAM2>]
            \t[-m|--mobility <DISTR> <PARAM1> <PARAM2>] [--seed <SEMILLA>] [-h|--help]
            Información sobre los argumentos:
            \t<ID>\t\tDefine el ID con el cual se identifica la entity UE.
            \t<X>\t\tDefine la coordenada X inicial donde se sitúa la entity UE.
            \t<Y>\t\tDefine la coordenada Y inicial donde se sitúa la entity UE.
            \t--host <IP> <PUERTO>
            \t\t\tDefine la IP o el nombre del host donde está ubicado el broker y su puerto.
            \t\t\tValor por defecto: localhost 3000.
            \t-s|--size <DISTR> <PARAM1> <PARAM2>
            \t\t\tDefine cómo se obtiene el valor tiempo demandado.
            \t\t\tValores permitidos para DISTR:
            \t\t\t\td[eterminista]\tsize=PARAM1
            \t\t\t\tu[niforme]\tE[size]=(PARAM1+PARAM2)/2
            \t\t\t\te[xponencial]\tE[size]=1/PARAM1
            \t\t\tValor por defecto: e 1 0.
            \t-d|--delay <DISTR> <PARAM1> <PARAM2>
            \t\t\tDefine cómo se obtiene el valor tiempo entre llegadas.
            \t\t\tValores permitidos para DISTR:
            \t\t\t\td[eterminista]\tdelay=PARAM1
            \t\t\t\tu[niforme]\tE[delay]=(PARAM1+PARAM2)/2
            \t\t\t\te[xponencial]\tE[delay]=1/PARAM1
            \t\t\tValor por defecto: e 1 0.
            \t-m|--mobility <DISTR> <PARAM1> <PARAM2>
            \t\t\tDefine cómo se obtiene el valor del desplazamiento entre generaciones.
            \t\t\tValores permitidos para DISTR:
            \t\t\t\td[eterminista]\tmobility=PARAM1
            \t\t\t\tu[niforme]\tE[mobility]=(PARAM1+PARAM2)/2
            \t\t\t\te[xponencial]\tE[mobility]=1/PARAM1
            \t\t\tValor por defecto: d 0 0.
            \t--seed <SEMILLA>La generación de números aleatorios estará basada en una semilla con valor=SEMILLA+ID.
            \t\t\tValor por defecto: La generación de números aleatorios no se basa en ninguna semilla.
            \t-h|--help\tImprime por consola la lista de posibles argumentos con su explicación.
            """;
    public final TreeMap<Long, Task> listaTasksPendientes = new TreeMap<>();
    protected double c = 1;
    protected double tToOff = 0;
    protected double tToOn = 0;
    protected double tHysterisis = 0;
    protected double algorithmParam = 1;
    protected algorithmMode algorithm = algorithmMode.NO_COALESCING;
    protected double q = 0;
    protected stateType state;
    protected stateType nextState;
    protected boolean procesando = false;
    protected Task currentTask;
    private Communicator communicator;

    public BaseStation(String[] args) {
        String ipBroker = "localhost";
        int puertoBroker = 3000;
        int id = Integer.parseInt(args[0]);
        double x = Double.parseDouble(args[1]);
        double y = Double.parseDouble(args[2]);

        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "--host" -> {
                    ipBroker = args[++i];
                    puertoBroker = Integer.parseInt(args[++i]);
                }
                case "-a", "--algorithm" -> {
                    algorithm = algorithmMode.getModeTypeByCode(args[++i].charAt(0));
                    algorithmParam = Double.parseDouble(args[++i]);
                }
                case "-t", "--times" -> {
                    tToOff = Double.parseDouble(args[++i]);
                    tToOn = Double.parseDouble(args[++i]);
                    tHysterisis = Double.parseDouble(args[++i]);
                }
                case "-c", "--capacity" -> c = Double.parseDouble(args[++i]);
                case "-h", "--help" -> {
                    logger.log(Level.INFO, HELP_MSG);
                    return;
                }
            }
            i++;
        }

        final String msg = MessageFormat.format("""
                        userequipment.UserEquipment iniciado con los parámetros:
                        \tid={} x={} y={} host={} port={} capacity={}
                        \ttToOff={} tToOn={} tHysteresis={}
                        \talgorithm={} algorithmParam={}""",
                id, x, y, ipBroker, puertoBroker, c, tToOff, tToOn, tHysterisis, algorithm, algorithmParam);

        logger.log(Level.CONFIG, msg);

        communicator = new Communicator(communicatorType.BASE_STATION, ipBroker, puertoBroker, id, x, y);
    }

    public static void main(String[] args) {
        new BaseStation(args).start();
    }

    @Override
    public void run() {

        while (true) {
            final MessageUnpacker message = communicator.receiveMessage();

            try {
                final actionType action = actionType.getActionTypeByCode(message.unpackInt());

                switch (action) {
                    case TRAFFIC_ARRIVE -> processTrafficArrival(message);
                    case TRAFFIC_EGRESS -> processTrafficEgress(message);
                    case NEW_STATE -> processNewState(message);
                    case CLOSE -> {
                        communicator.close();
                        return;
                    }
                    case UNADMITTED -> {
                        final String msg = "Received type of message not supported. Execution completed";
                        logger.log(Level.SEVERE, msg);
                        communicator.close();
                        System.exit(-1);
                    }
                }

            } catch (Exception e) {
                final String msg = "An attempt to pack / unpack a message failed. Execution completed";
                logger.log(Level.SEVERE, msg);
                communicator.close();
                System.exit(-1);
            }

        }

    }

    public void processTrafficArrival(MessageUnpacker request) throws IOException {

        final double t = request.unpackDouble();
        final long id = request.unpackLong();
        final double size = request.unpackDouble();
        request.close();

        final Task task = new Task(id, size, t);
        listaTasksPendientes.put(id, task);
        q += size;

        final double tNewState = Algorithm.activationAlgorithm(this, false);
        final double tTrafficEgress = Algorithm.processingAlgorithm(this);
        final double a = Task.getDelay(t);

        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(stateType.getCodeByStateType(state));
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(stateType.getCodeByStateType(nextState));
            response.packDouble(a).close();
            communicator.sendMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void processTrafficEgress(MessageUnpacker request) throws IOException {

        double t = request.unpackDouble();
        request.close();
        long id = currentTask.getId();
        double size = currentTask.getSize();
        double w = t - currentTask.gettArrive() - currentTask.getSize() / c;

        procesando = false;

        double tNewState = Algorithm.suspensionAlgorithm(this);
        double tTrafficEgress = Algorithm.processingAlgorithm(this);

        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q).packInt(stateType.getCodeByStateType(state)).packDouble(tTrafficEgress).packDouble(tNewState).packInt(stateType.getCodeByStateType(nextState))
                    .packDouble(w).packLong(id).packDouble(size).close();
            communicator.sendMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processNewState(MessageUnpacker request) throws IOException {

        int stateReceivedInt = request.unpackInt();
        request.close();

        double tNewState = 0;

        final stateType stateReceived = stateType.getStateTypeByCode(stateReceivedInt);

        switch (stateReceived) {
            case TO_OFF -> {
                nextState = stateType.OFF;
                tNewState = tToOff;
            }
            case TO_ON -> {
                nextState = stateType.ON;
                tNewState = tToOn;
            }
            case OFF -> tNewState = Algorithm.activationAlgorithm(this, true);
        }

        double tTrafficEgress = Algorithm.processingAlgorithm(this);

        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q).packInt(stateType.getCodeByStateType(stateReceived)).packDouble(tTrafficEgress).packDouble(tNewState).packInt(stateType.getCodeByStateType(nextState))
                    .close();
            communicator.sendMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}