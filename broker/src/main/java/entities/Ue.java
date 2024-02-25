package entities;

import communication.Communicator;
import domain.Task;
import domain.Position;

public class Ue extends Entity {

  private double eL = 0.0;
  private double eA = 0.0;
  private long taskCounter = 0;

  public Ue(Position position, Communicator communicator) {
    super(position, communicator);
  }

  public void addTask(Position position, Task task) {
    this.setPosition(position);
    eL += task.size();
    eA += task.tArrivalTime();
    taskCounter++;
  }

  public double geteL() {
    return taskCounter != 0 ? (eL / taskCounter) : 0;
  }

  public double geteA() {
    return taskCounter != 0 ? (eA / taskCounter) : 0;
  }
}
