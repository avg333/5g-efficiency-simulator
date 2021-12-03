package loggers;

import entities.Bs;
import entities.Ue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.CommunicatorType;
import types.EventType;
import types.StateType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class LoggerCustom {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerCustom.class);

    private static final String DIR = "/logs/events/";
    private static final int AVANCE = 1;
    private static final String[] COLUMNS = {"T", "ENTIDAD", "ID", "EVENTO", "TAREA", "L", "A", "X", "Y",
            "FROM-UE", "TO-BS", "Q", "W", "STATE"};
    private final boolean printCsv;
    private final SimpleDateFormat formatter;
    private int aux = AVANCE;
    private FileWriter out;
    private CSVPrinter printer;

    public LoggerCustom(boolean printCsv) {
        formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        this.printCsv = printCsv;
        if (printCsv) {
            try {
                File theDir = new File(DIR);
                if (!theDir.exists()) theDir.mkdirs();
                out = new FileWriter(DIR + "events_" + formatter.format(new Date()) + ".csv");
                printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(COLUMNS));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void printProgress(double current, double total) {
        if (current / total * 100 <= aux) {
            return;
        }

        aux += AVANCE;

        int percent = (int) (current * 100 / total);
        String string = '\r' +
                String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")) +
                String.format(" %d%% [", percent) + String.join("", Collections.nCopies(percent, "=")) +
                '>' + String.join("", Collections.nCopies(100 - percent, " ")) + ']' +
                String.join("",
                        Collections.nCopies(current == 0 ? (int) (Math.log10(total))
                                : (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")) +
                String.format(" %d/%d", (int) current, (int) total);

        System.out.print(string);
    }

    public void imprimirResultados(long elapsedTime, double t, Map<Integer, Bs> listaBS, Map<Integer, Ue> listaUE) {
        LOGGER.info("Fin de la simulacion. Tiempo de ejecucion: {}s", elapsedTime / 1000);
        imprimirResumen(elapsedTime, t, listaBS, listaUE);
    }

    public void imprimirResumen(long elapsedTime, double t, Map<Integer, Bs> listaBS, Map<Integer, Ue> listaUE) {
        double eQ = 0;
        double eW = 0;
        double eL = 0;
        double eA = 0;

        for (Map.Entry<Integer, Bs> entry : listaBS.entrySet()) {
            Bs bsAux = entry.getValue();
            eQ += bsAux.getEq();
            eW += bsAux.getEw();
        }

        eQ = eQ / listaBS.size();
        eW = eW / listaBS.size();

        for (Map.Entry<Integer, Ue> entry : listaUE.entrySet()) {
            Ue ueAux = entry.getValue();
            eL += ueAux.geteL();
            eA += ueAux.geteA();
        }

        eL = eL / listaUE.size();
        eA = eA / listaUE.size();

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter("resumen_" + formatter.format(new Date()) + ".txt"))) {
            writer.write("Resumen simulaci�n " + formatter.format(new Date()) + ":\n");
            writer.write("Eventos: " + printCsv + ". T final: " + (t)
                    + ". Tiempo de simulaci�n: " + (elapsedTime / 1000) + "s.\n");
            writer.write("N: " + listaBS.size() + ". E[Q] Global: " + (eQ) + ". E[W] Global: "
                    + (eW) + ".\n");
            for (Map.Entry<Integer, Bs> entry : listaBS.entrySet())
                writer.write(
                        "\tBS ID: " + entry.getValue().getId() + " E[Q]: " + (entry.getValue().getEq())
                                + " E[W]: " + (entry.getValue().getEw()) + ".\n");
            writer.write("m: " + listaUE.size() + ". E[L] Global: " + (eL) + ". E[A] Global: "
                    + (eA) + ".\n");
            for (Map.Entry<Integer, Ue> entry : listaUE.entrySet())
                writer.write(
                        "\tUE ID: " + entry.getValue().getId() + " E[L]: " + (entry.getValue().geteL())
                                + " E[A]: " + (entry.getValue().geteA()) + ".\n");
        } catch (IOException e) {
            LOGGER.error("Error al imprimir el archivo resumen.txt.", e);
        }
    }

    public void logTrafficIngress(double t, int idUe, double xUe, double yUe, long idTarea, double size,
                                  double delay) {
        LOGGER.debug("{} entity={} {} event={} id={} size={} next={} x={} y={}", t, CommunicatorType.USER_EQUIPMENT,
                idUe, EventType.TRAFFIC_INGRESS, idTarea, size, delay, xUe, yUe);
        if (printCsv) {
            try {
                printer.printRecord(t, CommunicatorType.USER_EQUIPMENT, idUe, EventType.TRAFFIC_INGRESS,
                        idTarea, size, delay, xUe, yUe, null, null, null, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void logTrafficRoute(double t, int idUe, int idBs, long idTask, double size) {
        LOGGER.debug("{} BK 0 TRAFFIC_ROUTE id={} size={} from-ue={} to-bs={}", t, idTask, size, idUe, idBs);
        if (printCsv) {
            try {
                printer.printRecord(t, CommunicatorType.BROKER, 0, EventType.TRAFFIC_ROUTE, idTask, size,
                        null, null, null, idUe, idBs, null, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void logTrafficArrival(double t, int idBs, long idTask, double size, double q, double a) {
        LOGGER.debug("{} BS {} TRAFFIC_ARRIVAL id={} size={} a={} q={}", t, idBs, idTask, size, a, q);
        if (printCsv) {
            try {
                printer.printRecord(t, CommunicatorType.BASE_STATION, idBs, EventType.TRAFFIC_ARRIVE, idTask,
                        size, a, null, null, null, null, q, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void logTrafficEgress(double t, int idBs, long idTask, double size, double q, double wait) {
        LOGGER.debug("{} BS {} TRAFFIC_EGRESS id={} size={} q={} wait={}", t, idBs, idTask, size, q, wait);
        if (printCsv) {
            try {
                printer.printRecord(t, CommunicatorType.BASE_STATION, idBs, EventType.TRAFFIC_EGRESS, idTask,
                        size, null, null, null, null, null, q, wait, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void logNewState(double t, int idBs, double q, StateType stateBs) {
        LOGGER.debug("{} BS {} NEW_STATE q={} state={}", t, idBs, q, stateBs);
        if (printCsv) {
            try {
                printer.printRecord(t, CommunicatorType.BASE_STATION, idBs, EventType.NEW_STATE, null, null,
                        null, null, null, null, null, q, null, stateBs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
