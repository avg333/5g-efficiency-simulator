package entities;

import communication.Communicator;
import types.StateType;

public class Bs extends Entity {

    private static int idCounter = 0;

    private double q = 0.0;
    private double eW = 0.0;
    private double eQ = 0.0;

    private StateType state = StateType.UNADMITTED;
    private StateType nextStateBs = StateType.OFF;
    private long idEventNextState;
    private long wCounter = 0;
    private double qAux = 0.0;
    private double tAux = 0.0;

    public Bs(double x, double y, Communicator communicator) {
        super(x, y, communicator);
        this.setId(idCounter++);
    }

    public double getQ() {
        return q;
    }

    public double getEw() {
        return wCounter != 0 ? (eW / wCounter) : 0;
    }

    public double getEq() {
        return tAux != 0 ? (eQ / tAux) : 0;
    }

    public StateType getState() {
        return state;
    }

    public void setState(StateType state) {
        this.state = state;
    }

    public StateType getNextStateBs() {
        return nextStateBs;
    }

    public void setNextState(StateType nextStateBs) {
        this.nextStateBs = nextStateBs;
    }

    public long getIdEventNextState() {
        return idEventNextState;
    }

    public void setIdEventNextState(long idEventNextState) {
        this.idEventNextState = idEventNextState;
    }

    public void addW(double w) {
        this.eW += w;
        this.wCounter++;
    }

    public void addQ(double q, double t) {
        final double interval = t - tAux;
        this.q = q;
        this.eQ += qAux * interval;
        this.qAux = q;
        this.tAux = t;
    }

}
