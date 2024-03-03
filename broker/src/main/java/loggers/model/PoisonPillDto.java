package loggers.model;

public class PoisonPillDto implements BaseDtoLog {

  @Override
  public final String getLogLine() {
    throw new UnsupportedOperationException("Poison pill should not be logged");
  }

  @Override
  public final BaseCsvDtoLog toCsvDtoLog() {
    throw new UnsupportedOperationException("Poison pill should not be logged");
  }
}
