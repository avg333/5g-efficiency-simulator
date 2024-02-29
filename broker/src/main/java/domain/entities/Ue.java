package domain.entities;

import communication.ClientCommunicator;
import domain.Position;
import domain.Task;

public class Ue extends Entity {

  private double eL = 0.0;
  private double eA = 0.0;
  private long taskCounter = 0;

  public Ue(Position position, ClientCommunicator communicator) {
    super(communicator);
    this.position = position;
  }

  public void addTask(final Position position, final Task task) {
    this.position = position;
    eL += task.size();
    eA += task.tUntilNextTask();
    taskCounter++;
  }

  public double geteL() {
    return taskCounter != 0 ? (eL / taskCounter) : 0;
  }

  public double geteA() {
    return taskCounter != 0 ? (eA / taskCounter) : 0;
  }
}
