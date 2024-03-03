package loggers.model;

public interface BaseDtoLog {

  String getLogLine();

  BaseCsvDtoLog toCsvDtoLog();
}
