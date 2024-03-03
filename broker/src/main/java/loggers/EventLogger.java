package loggers;

import static java.util.Objects.nonNull;
import static utils.BrokerUtils.getFileName;

import exception.CsvWriterException;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import loggers.model.BaseDtoLog;
import loggers.model.PoisonPillDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventLogger implements AutoCloseable {
  private static final String FILE_NAME = "events";
  private static final String EXTENSION = "csv";

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

  private void initializeCsvWriter() {
    try {
      eventPrinter = new EventPrinter(getFileName(FILE_NAME, EXTENSION));
    } catch (IOException e) {
      log.error("Failed to initialize CSV writer", e);
      throw new CsvWriterException("Failed to initialize CSV writer", e);
    }
  }

  private void start() {
    Thread.startVirtualThread(
        () -> {
          while (true) {
            if (runner()) return;
          }
        });
  }

  private boolean runner() {
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
    } catch (InterruptedException | IOException e) {
      log.error("Failed to process log", e);
      throw new RuntimeException(e);
    }
    return false;
  }

  public void log(final BaseDtoLog baseDtoLog) {
    if (printCsv || printLog) {
      queue.add(baseDtoLog);
    }
  }

  @Override
  public void close() {
    queue.add(new PoisonPillDto());
  }
}
