package loggers;

import static java.util.Objects.nonNull;

import exception.CsvWriterException;
import exception.LogQueueException;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import loggers.model.BaseDtoLog;
import loggers.model.PoisonPillDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventLogger implements AutoCloseable {

  private final boolean printCsv;
  private final boolean printLog;

  private final BlockingQueue<BaseDtoLog> queue = new LinkedBlockingQueue<>();

  private EventPrinter eventPrinter;

  public EventLogger(final boolean printCsv, final boolean printLog) {
    if (printCsv) {
      initializeCsvWriter();
      start();
    }
    this.printCsv = nonNull(eventPrinter);
    this.printLog = printLog;
  }

  public final void log(final BaseDtoLog baseDtoLog) {
    if (printCsv || printLog) {
      queue.add(baseDtoLog);
    }
  }

  @Override
  public final void close() {
    queue.add(new PoisonPillDto());
  }

  private void initializeCsvWriter() {
    try {
      eventPrinter = new EventPrinter();
    } catch (final IOException e) {
      log.error("Failed to initialize CSV writer", e);
      throw new CsvWriterException("Failed to initialize CSV writer", e);
    }
  }

  private void start() {
    Thread.startVirtualThread(
        () -> {
          boolean shouldContinue = true;
          while (shouldContinue) {
            shouldContinue = !logDaemon();
          }
        });
  }

  private boolean logDaemon() {
    try {
      final BaseDtoLog dto = queue.take();
      if (dto instanceof PoisonPillDto) {
        return true;
      }
      if (printLog) {
        log.debug(dto.getLogLine());
      }
      if (printCsv) {
        eventPrinter.printDtoLog(dto.toCsvDtoLog());
      }
    } catch (final InterruptedException e) {
      log.error("Error while taking log from queue", e);
      throw new LogQueueException(e);
    } catch (final IOException e) {
      log.error("Failed to print log", e);
      throw new CsvWriterException("Failed to print log", e);
    }
    return false;
  }
}
