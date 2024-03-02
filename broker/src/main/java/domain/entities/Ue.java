package domain.entities;

import communication.ClientCommunicator;
import domain.Position;
import domain.Task;

public class Ue extends Entity {

  private double eL = 0.0;
  private double eA = 0.0;
  private long taskCounter = 0;

  public Ue(final ClientCommunicator communicator, final Position position) {
    super(communicator);
    this.position = position;
  }

  public final void addTask(final Task task, final Position position) {
    this.position = position;
    eL += task.size();
    eA += task.tUntilNextTask();
    taskCounter++;
  }

  public final double geteL() {
    return taskCounter != 0 ? (eL / taskCounter) : 0;
  }

  public final double geteA() {
    return taskCounter != 0 ? (eA / taskCounter) : 0;
  }
}
