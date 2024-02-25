package exception;

public class CommunicatorCreationException extends RuntimeException {
  public CommunicatorCreationException(Throwable cause) {
    super("Error trying to create the communicator", cause);
  }
}
