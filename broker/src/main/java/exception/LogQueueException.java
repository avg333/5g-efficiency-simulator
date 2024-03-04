package exception;

public class LogQueueException extends RuntimeException {
  public LogQueueException(Throwable cause) {
    super("Error while taking log from queue", cause);
  }
}
