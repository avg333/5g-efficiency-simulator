package exception;

public class NoEventsAvailableException extends RuntimeException {
  public NoEventsAvailableException() {
    super("Event queue is empty");
  }
}
