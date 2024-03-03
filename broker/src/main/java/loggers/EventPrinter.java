package loggers;

import static utils.Utils.closeResource;

import java.io.FileWriter;
import java.io.IOException;
import loggers.model.BaseCsvDtoLog;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class EventPrinter implements AutoCloseable {

  private final FileWriter out;
  private final CSVPrinter csvPrinter;

  public EventPrinter(final String fileName) throws IOException {
    out = new FileWriter(fileName);
    csvPrinter =
        new CSVPrinter(out, CSVFormat.DEFAULT.builder().setHeader(BaseCsvDtoLog.COLUMNS).build());
  }

  public final void printDtoLog(final BaseCsvDtoLog dtoLog) throws IOException {
    csvPrinter.printRecord(dtoLog.getValues());
  }

  @Override
  public final void close() {
    closeResource(csvPrinter, "CSV printer");
    closeResource(out, "CSV writer");
  }
}
