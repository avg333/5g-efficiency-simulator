package loggers;

import static utils.BrokerUtils.getFileName;

import broker.BrokerState;
import domain.entities.Bs;
import domain.entities.Ue;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ResumePrinter {
  private static final String FILE_NAME = "resume";
  private static final String EXTENSION = "txt";

  private final BrokerState state;
  private final boolean printCsv;
  private final boolean printResume;

  public void print(final double elapsedTime) {
    if (!printResume) {
      return;
    }

    try (final BufferedWriter writer =
        new BufferedWriter(new FileWriter(getFileName(FILE_NAME, EXTENSION)))) {
      writer.write(getResume(state.getT(), elapsedTime));
      writer.write(getBssResume(state.getBsList()));
      for (Bs bs : state.getBsList()) {
        writer.write(getBsResume(bs));
      }
      writer.write(getUeTitle(state.getUeList()));
      for (Ue ue : state.getUeList()) {
        writer.write(getUeResume(ue));
      }
    } catch (final IOException e) {
      log.error("Failed to print {} file.", getFileName(FILE_NAME, EXTENSION), e);
    }
  }

  private String getResume(final double t, final double elapsedTime) {
    final String format =
        """
        Simulation %s resume
        Print CSV: %b Final t: %f. Simulation time: %f s
        """;
    return String.format(format, new Date(), printCsv, t, elapsedTime / 1000.0);
  }

  private static String getBssResume(final List<Bs> bsList) {
    final double eQ = bsList.stream().mapToDouble(Bs::getEq).average().orElse(0);
    final double eW = bsList.stream().mapToDouble(Bs::getEw).average().orElse(0);
    final String format = "N: %d. E[Q] Global: %f E[W] Global: %f\n";
    return String.format(format, bsList.size(), eQ, eW);
  }

  private static String getBsResume(final Bs bs) {
    final String format = "\tBS ID: %s E[Q]: %s E[W]: %s.\n";
    return String.format(format, bs.getId(), bs.getEq(), bs.getEw());
  }

  private static String getUeTitle(final List<Ue> ueList) {
    final double eL = ueList.stream().mapToDouble(Ue::geteL).average().orElse(0);
    final double eA = ueList.stream().mapToDouble(Ue::geteA).average().orElse(0);
    final String format = "m: %d. E[L] Global: %f. E[A] Global: %f.\n";
    return String.format(format, ueList.size(), eL, eA);
  }

  private static String getUeResume(final Ue ue) {
    final String format = "\tUE ID: %s E[L]: %s E[A]: %s.\n";
    return String.format(format, ue.getId(), ue.geteL(), ue.geteA());
  }
}
