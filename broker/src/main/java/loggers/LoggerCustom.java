package loggers;

import broker.EventType;
import domain.Task;
import entities.Bs;
import entities.Ue;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.BsStateType;
import types.EntityType;

public class LoggerCustom {

  private static final int ADVANCE = 1;
  private static final String[] COLUMNS = {
    "T", "ENTITY", "ID", "EVENT", "TASK", "L", "A", "X", "Y", "FROM-UE", "TO-BS", "Q", "W", "STATE"
  };

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final boolean printCsv;
  private final SimpleDateFormat formatter;
  private int aux = ADVANCE;
  private FileWriter out;
  private CSVPrinter printer;

  public LoggerCustom(boolean printCsv) {
    formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    this.printCsv = printCsv;
    if (printCsv) {
      try {
        out = new FileWriter("events_" + formatter.format(new Date()) + ".csv");
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

    aux += ADVANCE;

    int percent = (int) (current * 100 / total);
    String string =
        '\r'
            + String.join(
                "", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " "))
            + String.format(" %d%% [", percent)
            + String.join("", Collections.nCopies(percent, "="))
            + '>'
            + String.join("", Collections.nCopies(100 - percent, " "))
            + ']'
            + String.join(
                "",
                Collections.nCopies(
                    current == 0
                        ? (int) (Math.log10(total))
                        : (int) (Math.log10(total)) - (int) (Math.log10(current)),
                    " "))
            + String.format(" %d/%d", (int) current, (int) total);

    System.out.print(string);
  }

  public void printResults(long elapsedTime, double t, List<Bs> bsList, List<Ue> ueList) {
    log.info("End of simulation. Execution time: {}s", elapsedTime / 1000);
    printResume(elapsedTime, t, bsList, ueList);
    close();
  }

  private void printResume(long elapsedTime, double t, List<Bs> bsList, List<Ue> ueList) {
    double eQ = 0;
    double eW = 0;
    double eL = 0;
    double eA = 0;

    for (Bs bs : bsList) {
      eQ += bs.getEq();
      eW += bs.getEw();
    }

    eQ = eQ / bsList.size();
    eW = eW / bsList.size();

    for (Ue ue : ueList) {
      eL += ue.geteL();
      eA += ue.geteA();
    }

    eL = eL / ueList.size();
    eA = eA / ueList.size();

    try (final BufferedWriter writer =
        new BufferedWriter(new FileWriter("resume_" + formatter.format(new Date()) + ".txt"))) {
      final String resume =
          """
                    Simulation %s resume
                    Print CSV: %b Final t: %f. Simulation time: %d s
                    N: %d. E[Q] Global: %f E[W] Global: %f
                    """
              .formatted(new Date(), printCsv, t, elapsedTime / 1000, bsList.size(), eQ, eW);
      writer.write(resume);
      for (Bs bs : bsList) {
        writer.write(
            "\tBS ID: " + bs.getId() + " E[Q]: " + bs.getEq() + " E[W]: " + bs.getEw() + ".\n");
      }
      writer.write("m: " + ueList.size() + ". E[L] Global: " + eL + ". E[A] Global: " + eA + ".\n");
      for (Ue ue : ueList) {
        writer.write(
            "\tUE ID: " + ue.getId() + " E[L]: " + ue.geteL() + " E[A]: " + ue.geteA() + ".\n");
      }
    } catch (IOException e) {
      log.error("Failed to print resume.txt file.", e);
    }
  }

  private void close() {
    try {
      if (out != null) {
        out.close();
      }
      if (printer != null) {
        printer.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void logTrafficIngress(double t, Ue ue, Task task) {
    log.debug(
        "{} entity={} {} event={} id={} size={} next={} x={} y={}",
        t,
        EntityType.USER_EQUIPMENT,
        ue.getId(),
        EventType.TRAFFIC_INGRESS,
        task.id(),
        task.size(),
        task.tUntilNextTask(),
        ue.getPosition().getX(),
        ue.getPosition().getY());
    if (printCsv) {
      try {
        printer.printRecord(
            t,
            EntityType.USER_EQUIPMENT,
            ue.getId(),
            EventType.TRAFFIC_INGRESS,
            task.id(),
            task.size(),
            task.tUntilNextTask(),
            ue.getPosition().getX(),
            ue.getPosition().getY(),
            null,
            null,
            null,
            null,
            null);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void logTrafficRoute(double t, Ue ue, Bs bs, Task task) {
    log.debug(
        "{} BK 0 TRAFFIC_ROUTE id={} size={} from-ue={} to-bs={}",
        t,
        task.id(),
        task.size(),
        ue.getId(),
        bs.getId());
    if (printCsv) {
      try {
        printer.printRecord(
            t,
            EntityType.BROKER,
            0,
            EventType.TRAFFIC_ROUTE,
            task.id(),
            task.size(),
            null,
            null,
            null,
            ue.getId(),
            bs.getId(),
            null,
            null,
            null);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void logTrafficArrival(double t, Bs bs, Task task, double q, double a) {
    log.debug(
        "{} BS {} TRAFFIC_ARRIVAL id={} size={} a={} q={}",
        t,
        bs.getId(),
        task.id(),
        task.size(),
        a,
        q);
    if (printCsv) {
      try {
        printer.printRecord(
            t,
            EntityType.BASE_STATION,
            bs.getId(),
            EventType.TRAFFIC_ARRIVE,
            task.id(),
            task.size(),
            a,
            null,
            null,
            null,
            null,
            q,
            null,
            null);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void logTrafficEgress(
      double t, int idBs, long idTask, double size, double q, double wait) {
    log.debug("{} BS {} TRAFFIC_EGRESS id={} size={} q={} wait={}", t, idBs, idTask, size, q, wait);
    if (printCsv) {
      try {
        printer.printRecord(
            t,
            EntityType.BASE_STATION,
            idBs,
            EventType.TRAFFIC_EGRESS,
            idTask,
            size,
            null,
            null,
            null,
            null,
            null,
            q,
            wait,
            null);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void logNewState(double t, Bs bs, double q, BsStateType stateBs) {
    log.debug("{} BS {} NEW_STATE q={} state={}", t, bs.getId(), q, stateBs);
    if (printCsv) {
      try {
        printer.printRecord(
            t,
            EntityType.BASE_STATION,
            bs.getId(),
            EventType.NEW_STATE,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            q,
            null,
            stateBs);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public String toString() {
    return printCsv ? "csv printer enabled" : "csv printer disabled";
  }
}
