package entities;

import communication.Communicator;
import types.BsStateType;

public class Bs extends Entity {

    private double q = 0.0;
    private double eW = 0.0;
    private double eQ = 0.0;

    private BsStateType state = BsStateType.OFF;
    private BsStateType nextStateBs = BsStateType.OFF;
    private long idEventNextState;
    private long wCounter = 0;
    private double qAux = 0.0;
    private double tAux = 0.0;

    public Bs(double x, double y, Communicator communicator) {
        super(x, y, communicator);
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

    public BsStateType getState() {
        return state;
    }

    public void setState(BsStateType state) {
        this.state = state;
    }

    public BsStateType getNextStateBs() {
        return nextStateBs;
    }

    public void setNextState(BsStateType nextStateBs) {
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
