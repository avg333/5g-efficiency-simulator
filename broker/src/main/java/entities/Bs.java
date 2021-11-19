package entities;

import types.stateType;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class Bs extends Entity {

	private double q = 0.0;
	private double eW = 0.0;
	private double eQ = 0.0;

	private stateType state = stateType.UNADMITTED;
	private stateType nextStateBs = stateType.OFF;
	private long idEventNextState;
	private long wCounter = 0;
	private double qAux = 0.0;
	private double tAux = 0.0;

	public Bs(int id, double x, double y, DatagramSocket sc, InetAddress ad, int puerto) {
		super(id, x, y, sc, ad, puerto);
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

	public stateType getState() {
		return state;
	}

	public void setState(stateType state) {
		this.state = state;
	}

	public stateType getNextStateBs() {
		return nextStateBs;
	}

	public void setNextState(stateType nextStateBs) {
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
