package exception;

import domain.EventType;

public class InvalidEventTypeException extends RuntimeException {
  public InvalidEventTypeException(EventType eventType) {
    super("Invalid event type: " + eventType);
  }
}
