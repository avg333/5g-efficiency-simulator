package exception;

public class NoCurrentTaskException extends IllegalStateException {
  public NoCurrentTaskException() {
    super("There is no tasks to be processed in the queue");
  }
}
