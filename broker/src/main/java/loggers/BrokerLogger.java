package loggers;

import broker.BrokerState;
import loggers.model.BaseDtoLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrokerLogger implements AutoCloseable {
  private final double startTime = System.currentTimeMillis();
  private final boolean printResume;
  private final boolean printCsv;
  private final BrokerState state;
  private final EventLogger eventLogger;
  private final ProgressBarLogger progressBarLogger;

  public BrokerLogger(
      final boolean printResume,
      final boolean printCsv,
      final boolean progressBar,
      final double finalT,
      final BrokerState state) {
    this.printResume = printResume;
    this.printCsv = printCsv;
    this.state = state;
    this.eventLogger = new EventLogger(printCsv, log.isDebugEnabled());
    this.progressBarLogger = new ProgressBarLogger(progressBar, finalT);
  }

  public void upgradeProgress(final double t) {
    progressBarLogger.upgradeProgress(t);
  }

  public void log(final BaseDtoLog baseDtoLog) {
    eventLogger.log(baseDtoLog);
  }

  @Override
  public void close() {
    final double executionTime = System.currentTimeMillis() - startTime;
    log.info("End of simulation. Execution time: {}s", executionTime / 1000);
    if (printResume) {
      new ResumePrinter(printCsv).print(executionTime, state);
    }
    progressBarLogger.close();
    eventLogger.close();
  }
}
