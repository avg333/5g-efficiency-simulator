package entities;

import communication.Communicator;

public class Ue extends Entity {

    private double eL = 0.0;
    private double eA = 0.0;
    private long taskCounter = 0;

    public Ue(double x, double y, Communicator communicator) {
        super(x, y, communicator);
    }

    public void addTask(double x, double y, double l, double a) {
        setX(x);
        setY(y);
        eL += l;
        eA += a;
        taskCounter++;
    }

    public double geteL() {
        return taskCounter != 0 ? (eL / taskCounter) : 0;
    }

    public double geteA() {
        return taskCounter != 0 ? (eA / taskCounter) : 0;
    }

}
