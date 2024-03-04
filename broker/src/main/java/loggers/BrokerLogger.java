package loggers;

import broker.BrokerConfig;
import broker.BrokerState;
import loggers.model.BaseDtoLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrokerLogger implements AutoCloseable {
  private final double startTime = System.currentTimeMillis();
  private final ResumePrinter resumePrinter;
  private final EventLogger eventLogger;
  private final ProgressBarLogger progressBarLogger;

  public BrokerLogger(final BrokerConfig config, final BrokerState state) {
    this.resumePrinter = new ResumePrinter(state, config.printCsv(), config.printResume());
    this.eventLogger = new EventLogger(config.printCsv(), log.isDebugEnabled());
    this.progressBarLogger = new ProgressBarLogger(config.progressBar(), config.finalT());
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
    resumePrinter.print(executionTime);
    progressBarLogger.close();
    eventLogger.close();
    log.info("End of simulation. Execution time: {}s", executionTime / 1000);
  }
}
