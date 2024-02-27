package entities;

import communication.Communicator;
import domain.Position;
import lombok.Getter;
import lombok.Setter;
import types.BsStateType;

public class Bs extends Entity {

  private double q = 0.0;
  private double eW = 0.0;
  private double eQ = 0.0;

  @Getter @Setter private BsStateType state = BsStateType.OFF;
  @Setter @Getter private BsStateType nextStateBs = BsStateType.OFF;
  @Setter @Getter private long idEventNextState;
  private long wCounter = 0;
  private double qAux = 0.0;
  private double tAux = 0.0;

  public Bs(Position position, Communicator communicator) {
    super(communicator);
    this.position = position;
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
