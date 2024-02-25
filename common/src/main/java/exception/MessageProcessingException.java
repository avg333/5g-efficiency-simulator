package exception;

public class MessageProcessingException extends RuntimeException {
  public MessageProcessingException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
