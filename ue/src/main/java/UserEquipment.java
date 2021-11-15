import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

enum distributionMode {
    DETERMINISTIC('d'), UNIFORM('u'), EXPONENTIAL('e'), UNADMITTED('x');

    private final char value;
    distributionMode(final char value) {
        this.value = value;
    }

    public static distributionMode getDistributionModeByCode(char code){
        for(distributionMode e : distributionMode.values()){
            if(code == e.value) return e;
        }
        return UNADMITTED;
    }
}

enum actionType {
    TRAFFIC_INGRESS(1), CLOSE(-1), UNADMITTED(0);

    private final int value;
    actionType(final int value) {
        this.value = value;
    }

    public static actionType getActionTypeByCode(int code){
        for(actionType e : actionType.values()){
            if(code == e.value) return e;
        }
        return UNADMITTED;
    }
}

public class UserEquipment extends Thread {

    private static final Logger logger = Logger.getLogger(UserEquipment.class.getName());

    private final Random rand = new Random();
    private DatagramSocket sc;
    private DatagramPacket dp;
    private double x;
    private double y;
    private distributionMode sizeDist = distributionMode.EXPONENTIAL;
    private distributionMode delayDist = distributionMode.EXPONENTIAL;
    private distributionMode mobilityDist = distributionMode.DETERMINISTIC;
    private double sizeParam1 = 1.0;
    private double sizeParam2 = 0.0;
    private double delayParam1 = 1.0;
    private double delayParam2 = 0.0;
    private double mobilityParam1 = 0.0;
    private double mobilityParam2 = 0.0;

    private double size = -1.0;
    private double delay = 0.0;

    public UserEquipment(String[] args) {
        if (args.length < 3) {
            final String msg = "Error al realizar el registro. Ejecución finalizada";
            logger.log(Level.SEVERE, msg);
            System.exit(-1);
        }

        String ipBroker = "localhost";
        int puertoBroker = 3000;
        long semilla = 0;
        int id = Integer.parseInt(args[0]);
        x = Double.parseDouble(args[1]);
        y = Double.parseDouble(args[2]);

        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "--host" -> {
                    ipBroker = args[++i];
                    puertoBroker = Integer.parseInt(args[++i]);
                }
                case "-s", "--size" -> {
                    sizeDist = distributionMode.getDistributionModeByCode(args[++i].charAt(0));
                    sizeParam1 = Double.parseDouble(args[++i]);
                    sizeParam2 = Double.parseDouble(args[++i]);
                }
                case "-d", "--delay" -> {
                    delayDist = distributionMode.getDistributionModeByCode(args[++i].charAt(0));
                    delayParam1 = Double.parseDouble(args[++i]);
                    delayParam2 = Double.parseDouble(args[++i]);
                }
                case "-m", "--mobility" -> {
                    mobilityDist = distributionMode.getDistributionModeByCode(args[++i].charAt(0));
                    mobilityParam1 = Double.parseDouble(args[++i]);
                    mobilityParam2 = Double.parseDouble(args[++i]);
                }
                case "--seed" -> {
                    semilla = Long.parseLong(args[++i]);
                    rand.setSeed(semilla + id);
                }
                case "-h", "--help" -> {
                    final String msg = """
                            Argumentos obligatorios:
                            \t<ID> <X> <Y>
                            Argumentos opcionales:
                            \t[--host <IP> <PUERTO>] [-s|--size <DISTR> <PARAM1> <PARAM2>] [-d|--delay <DISTR> <PARAM1> <PARAM2>]
                            \t[-m|--mobility <DISTR> <PARAM1> <PARAM2>] [--seed <SEMILLA>] [-h|--help]
                            Información sobre los argumentos:
                            \t<ID>\t\tDefine el ID con el cual se identifica la entidad UE.
                            \t<X>\t\tDefine la coordenada X inicial donde se sitúa la entidad UE.
                            \t<Y>\t\tDefine la coordenada Y inicial donde se sitúa la entidad UE.
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
                    logger.log(Level.INFO, msg);
                    return;
                }
            }
            i++;

        }

        final String msg = MessageFormat.format("""
                        UserEquipment iniciado con los parámetros:
                        \tid={} x={} y={} host={} port={} seed={}
                        \tsizeDist={} sizeParam1={} sizeParam2={}
                        \tdelayDist={} delayParam1={} delayParam2={}
                        \tmobilityDist={} mobilityParam1={} mobilityParam2={}""",
                id, x, y, ipBroker, puertoBroker, semilla, sizeDist, sizeParam1, sizeParam2, delayDist, delayParam1,
                delayParam2, mobilityDist, mobilityParam1, mobilityParam2);

        logger.log(Level.CONFIG, msg);

        register(ipBroker, puertoBroker, id);
    }

    public static void main(String[] args) {
        new UserEquipment(args).start();
    }

    private void register(final String ipBroker, final int portBroker, final int id) {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(1).packInt(id).packDouble(x).packDouble(y).close();
            final byte[] message = packer.toByteArray();
            final InetAddress ad = InetAddress.getByName(ipBroker);
            dp = new DatagramPacket(message, message.length, ad, portBroker);
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

        delay = getRandom(delayDist, delayParam1, delayParam2);

        while (true) {
            final actionType action = receiveMessage();

            switch (action) {
                case TRAFFIC_INGRESS -> processTrafficIngress();
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
        }

    }

    private void processTrafficIngress() {
        sendTask();
        x += getRandom(mobilityDist, mobilityParam1, mobilityParam2);
        y += getRandom(mobilityDist, mobilityParam1, mobilityParam2);
        size = getRandom(sizeDist, sizeParam1, sizeParam2);
        delay = getRandom(delayDist, delayParam1, delayParam2);
    }

    private actionType receiveMessage() {
        try {
            final byte[] data = new byte[10];
            sc.receive(new DatagramPacket(data, data.length));
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
            final int action = unpacker.unpackInt();
            unpacker.close();
            return actionType.getActionTypeByCode(action);
        } catch (IOException ex) {
            final String msg = "Error trying to receive a message. Execution completed";
            logger.log(Level.SEVERE, msg);
            System.exit(-1);
        }

        return actionType.UNADMITTED;
    }

    private void sendTask() {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packDouble(x).packDouble(y).packDouble(size).packDouble(delay).close();
            final byte[] message = packer.toByteArray();
            dp.setData(message, 0, message.length);
            sc.send(dp);
        } catch (IOException ex) {
            final String msg = "Error trying to send a message. Execution completed";
            logger.log(Level.SEVERE, msg);
            System.exit(-1);
        }
    }

    private double getRandom(distributionMode distribution, double param1, double param2) {
        return switch (distribution) {
            case DETERMINISTIC -> param1;
            case UNIFORM -> rand.nextDouble() * (param1 - param2) + param2;
            case EXPONENTIAL -> param1 != 0 ? Math.log(1 - rand.nextDouble()) / (-param1) : 0;
            case UNADMITTED -> 0;
        };

    }

}