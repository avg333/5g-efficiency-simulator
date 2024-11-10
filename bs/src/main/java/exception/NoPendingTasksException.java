package exception;

public class NoPendingTasksException extends IllegalStateException {
  public NoPendingTasksException() {
    super("There is no task in processing to be processed");
  }
}
