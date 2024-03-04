package basestation;

import static java.util.Objects.isNull;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Getter;
import lombok.Setter;
import task.Task;
import types.BsStateType;

public class BaseStationState {
  private static final BsStateType DEFAULT_STATE = BsStateType.OFF;
  private final Deque<Task> tasksPending = new ArrayDeque<>();
  private double lastTaskArrivalTime = 0.0;
  private Task currentTask = null;

  @Getter @Setter private BsStateType state = DEFAULT_STATE;
  @Getter @Setter private BsStateType nextState = DEFAULT_STATE;

  public double addTask(final Task task) {
    final double previousLastTaskArrivalTime = lastTaskArrivalTime;
    tasksPending.add(task);
    lastTaskArrivalTime = task.tArrivalTime();
    return lastTaskArrivalTime - previousLastTaskArrivalTime;
  }

  public Task processNextTask() {
    currentTask = tasksPending.poll();
    return currentTask;
  }

  public Task processCurrentTask() {
    final Task processedTask = currentTask;
    currentTask = null;
    return processedTask;
  }

  public boolean isCurrentState(final BsStateType state) {
    return this.state == state;
  }

  public boolean isIdle() {
    return isNull(currentTask);
  }

  public boolean hasTasksPending() {
    return !tasksPending.isEmpty();
  }

  public double getQ() { // TODO Add q sum to avoid recalculation
    return tasksPending.stream().mapToDouble(Task::size).sum();
  }
}
