package userequipment;

import types.Communicator;
import types.actionType;
import types.communicatorType;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserEquipment extends Thread {

    private static final Logger LOGGER = Logger.getLogger(UserEquipment.class.getName());
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
    private Communicator communicator;
    private Distribution mobilityDist;
    private Distribution sizeDist;
    private Distribution delayDist;
    private double x;
    private double y;
    private double size = -1.0;
    private double delay = 0.0;

    public UserEquipment(String[] args) {
        if (args.length < 3) {
            final String msg = "Error al realizar el registro. Ejecución finalizada";
            LOGGER.log(Level.SEVERE, msg);
            System.exit(-1);
        }

        String ipBroker = "localhost";
        int puertoBroker = 3000;
        long seed = 0;
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
                    distributionMode mode = distributionMode.getDistributionModeByCode(args[++i].charAt(0));
                    double param1 = Double.parseDouble(args[++i]);
                    double param2 = Double.parseDouble(args[++i]);
                    sizeDist = new Distribution(mode, param1, param2);
                }
                case "-d", "--delay" -> {
                    distributionMode mode = distributionMode.getDistributionModeByCode(args[++i].charAt(0));
                    double param1 = Double.parseDouble(args[++i]);
                    double param2 = Double.parseDouble(args[++i]);
                    delayDist = new Distribution(mode, param1, param2);
                }
                case "-m", "--mobility" -> {
                    distributionMode mode = distributionMode.getDistributionModeByCode(args[++i].charAt(0));
                    double param1 = Double.parseDouble(args[++i]);
                    double param2 = Double.parseDouble(args[++i]);
                    mobilityDist = new Distribution(mode, param1, param2);
                }
                case "--seed" -> {
                    seed = Long.parseLong(args[++i]) + id;
                    sizeDist.setSeed(seed);
                    delayDist.setSeed(seed);
                    mobilityDist.setSeed(seed);
                }
                case "-h", "--help" -> {
                    LOGGER.log(Level.INFO, HELP_MSG);
                    return;
                }
            }
            i++;

        }

        final String msg = MessageFormat.format("""
                        UserEquipment iniciado con los parámetros:
                        \tid={} x={} y={} host={} port={} seed={}""",
                id, x, y, ipBroker, puertoBroker, seed);

        LOGGER.log(Level.CONFIG, msg);

        communicator = new Communicator(communicatorType.USER_EQUIPMENT, ipBroker, puertoBroker, id, this.x, this.y);
    }

    public static void main(String[] args) {
        new UserEquipment(args).start();
    }

    @Override
    public void run() {

        delay = delayDist.getRandom();

        while (true) {
            final actionType action = communicator.receiveActionType();

            switch (action) {
                case TRAFFIC_INGRESS -> processTrafficIngress();
                case CLOSE -> {
                    communicator.close();
                    return;
                }
                case UNADMITTED -> {
                    final String msg = "Received type of message not supported. Execution completed";
                    LOGGER.log(Level.SEVERE, msg);
                    communicator.close();
                    System.exit(-1);
                }
            }
        }

    }

    private void processTrafficIngress() {
        communicator.sendTask(x, y, size, delay);
        x += mobilityDist.getRandom();
        y += mobilityDist.getRandom();
        size = sizeDist.getRandom();
        delay = delayDist.getRandom();
    }

}