package exception;

public class CsvWriterException extends RuntimeException {

  public CsvWriterException(String message, Throwable cause) {
    super(message, cause);
  }
}
