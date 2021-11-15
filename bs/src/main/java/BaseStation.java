import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

enum actionType {
    TRAFFIC_INGRESS(1), TRAFFIC_ARRIVE(2), TRAFFIC_EGRESS(3), NEW_STATE(4), CLOSE(-1), UNADMITTED(0);

    private final int value;

    actionType(final int value) {
        this.value = value;
    }

    public static actionType getActionTypeByCode(int code) {
        for (actionType e : actionType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }

    public static int getCodeByActionType(actionType action) {
        for (actionType e : actionType.values()) {
            if (action == e) return e.value;
        }
        return 0;
    }
}

enum stateType {
    ON(1), OFF(2), TO_ON(3), TO_OFF(4), HISTERISIS(-1), WAITING_TO_ON(-2), UNADMITTED(0);

    private final int value;

    stateType(final int value) {
        this.value = value;
    }

    public static stateType getStateTypeByCode(int code) {
        for (stateType e : stateType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }

    public static int getCodeByStateType(stateType state) {
        for (stateType e : stateType.values()) {
            if (state == e) return e.value;
        }
        return 0;
    }
}

public class BaseStation extends Thread {

    private static final Logger logger = Logger.getLogger(BaseStation.class.getName());
    public final TreeMap<Long, Task> listaTasksPendientes = new TreeMap<>();
    protected double c = 1;
    protected double tToOff = 0;
    protected double tToOn = 0;
    protected double tHysterisis = 0;
    protected double algorithmParam = 1;
    protected modeType algorithm = modeType.NO_COALESCING;
    protected double q = 0;
    protected stateType state;
    protected stateType nextState;
    protected boolean procesando = false;
    protected Task currentTask;
    private DatagramSocket sc;
    private DatagramPacket dp;

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
                    algorithm = modeType.getModeTypeByCode(args[++i].charAt(0));
                    algorithmParam = Double.parseDouble(args[++i]);
                }
                case "-t", "--times" -> {
                    tToOff = Double.parseDouble(args[++i]);
                    tToOn = Double.parseDouble(args[++i]);
                    tHysterisis = Double.parseDouble(args[++i]);
                }
                case "-c", "--capacity" -> c = Double.parseDouble(args[++i]);
                case "-h", "--help" -> {
                    final String msg = """
                            Argumentos obligatorily:
                            \t<ID> <X> <Y>
                            Argumentos opcionales:
                            \t[--host <IP> <PUERTO>] [-c|--capacity <CAPACIDAD>] [-t|--times <TTOOFF> <TTOON> <THISTERISIS>]
                            \t[-a|--algorithm <ALGORITMO> <PARAM>] [-h|--help]
                            Información sobre los argumentos:
                            \t<ID>\t\tDefine el ID con el cual se identifica la entidad BS.
                            \t<X>\t\tDefine la coordenada X inicial donde se sitúa la entidad BS.
                            \t<Y>\t\tDefine la coordenada Y inicial donde se sitúa la entidad BS.
                            \t--host <IP> <PUERTO>
                            \t\t\tDefine la IP o el nombre del host donde está ubicado el broker y su puerto.
                            \t\t\tValor por defecto: localhost 3000.
                            \t-c|--capacity <CAPACIDAD>
                            \t\t\tDefine la velocidad de procesamiento de Tasks.
                            \t\t\tValor por defecto: 1.
                            \t-t|--times <TTOOFF> <TTOON> <THISTERISIS>
                            \t\t\tDefine el tiempo de desactivación, activación e histéresis respectivamente.
                            \t\t\tValor por defecto: 0 0 0.
                            \t-a|--algorithm <ALGORITMO>  <PARAM>
                            \t\t\tDefine el comportamiento del algoritmo de decisión de estado. Valores permitidos para ALGORITMO:
                            \t\t\t\tn[o coalescing]
                            \t\t\t\ts[ize based coalescing] (umbralON=PARAM)
                            \t\t\t\tt[ime based coalescing] (temporizador=PARAM)
                            \t\t\t\tf[ixed coalescing]\t(periodo=PARAM)
                            \t\t\tValor por defecto: n 1.
                            \t-h|--help\tImprime por consola la lista de posibles argumentos con su explicación.
                            """;
                    logger.log(Level.INFO, msg);
                    return;
                }
            }
            i++;
        }


        final String msg = MessageFormat.format("""
                        UserEquipment iniciado con los parámetros:
                        \tid={} x={} y={} host={} port={} capacity={}
                        \ttToOff={} tToOn={} tHysteresis={}
                        \talgorithm={} algorithmParam={}""",
                id, x, y, ipBroker, puertoBroker, c, tToOff, tToOn, tHysterisis, algorithm, algorithmParam);

        logger.log(Level.CONFIG, msg);

        register(ipBroker, puertoBroker, id, x, y);
    }

    public static void main(String[] args) {
        new BaseStation(args).start();
    }

    private void register(String ipBroker, int puertoBroker, int id, double x, double y) {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(2).packInt(id).packDouble(x).packDouble(y).close();
            final byte[] mensaje = packer.toByteArray();
            final InetAddress ad = InetAddress.getByName(ipBroker);
            dp = new DatagramPacket(mensaje, mensaje.length, ad, puertoBroker);
            sc = new DatagramSocket();
            sc.send(dp);
        } catch (IOException ex) {
            final String msg = "Registration failed. Execution completed";
            logger.log(Level.SEVERE, msg, ex);
            System.exit(-1);
        }

    }

    @Override
    public void run() {

        while (true) {
            final MessageUnpacker message = receiveMessage();

            try {
                final actionType action = actionType.getActionTypeByCode(message.unpackInt());

                switch (action) {
                    case TRAFFIC_ARRIVE -> procesarTrafficArrival(message);
                    case TRAFFIC_EGRESS -> procesarTrafficEgress(message);
                    case NEW_STATE -> procesarNewState(message);
                    case CLOSE -> {
                        sc.close();
                        return;
                    }
                    case UNADMITTED -> {
                        final String msg = "Received type of message not supported. Execution completed";
                        logger.log(Level.SEVERE, msg);
                        sc.close();
                        System.exit(-1);
                    }
                }

            } catch (Exception e) {
                final String msg = "An attempt to pack / unpack a message failed. Execution completed";
                logger.log(Level.SEVERE, msg);
                sc.close();
                System.exit(-1);
            }

        }

    }

    private MessageUnpacker receiveMessage() {
        MessageUnpacker unpacker = null;
        try {
            final byte[] data = new byte[50];
            sc.receive(new DatagramPacket(data, data.length));
            unpacker = MessagePack.newDefaultUnpacker(data);
        } catch (IOException ex) {
            final String msg = "Error trying to receive a message. Execution completed";
            logger.log(Level.SEVERE, msg);
            System.exit(-1);
        }

        return unpacker;
    }

    private void sendMessage(final MessageBufferPacker packer) {
        try {
            final byte[] message = packer.toByteArray();
            dp.setData(message, 0, message.length);
            sc.send(dp);
        } catch (IOException ex) {
            final String msg = "Error trying to receive a message. Execution completed";
            logger.log(Level.SEVERE, msg);
            System.exit(-1);
        }
    }

    public void procesarTrafficArrival(MessageUnpacker request) throws IOException {

        double t = request.unpackDouble();
        long id = request.unpackLong();
        double size = request.unpackDouble();
        request.close();

        Task task = new Task(id, size, t);
        listaTasksPendientes.put(id, task);
        q += size;

        double tNewState = Algorithm.activationAlgorithm(this, false);
        double tTrafficEgress = Algorithm.processingAlgorithm(this);
        double a = Task.getDelay(t);

        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q).packInt(stateType.getCodeByStateType(state)).packDouble(tTrafficEgress).packDouble(tNewState).packInt(stateType.getCodeByStateType(nextState))
                    .packDouble(a).close();
            sendMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void procesarTrafficEgress(MessageUnpacker request) throws IOException {

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
            sendMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void procesarNewState(MessageUnpacker request) throws IOException {

        int estadoRecibido = request.unpackInt();
        request.close();

        double tNewState = 0;

        final stateType stateReceived = stateType.getStateTypeByCode(estadoRecibido);

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
            sendMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}