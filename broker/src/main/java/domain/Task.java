package domain;

import static types.Constants.NO_TASK_TO_PROCESS;

public record Task(long id, double size, double tArrivalTime, double tUntilNextTask) {

  private static long taskCounter = 0;

  public static Task createNewTask(double size, double tArrivalTime, double tUntilNextTask) {
    return new Task(taskCounter++, size, tArrivalTime, tUntilNextTask);
  }

  public boolean isEmpty() {
    return size == NO_TASK_TO_PROCESS.getValue();
  }
}
