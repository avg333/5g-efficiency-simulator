package domain.entities;

import communication.ClientCommunicator;
import domain.Position;
import lombok.Getter;
import lombok.Setter;
import types.BsStateType;

public class Bs extends Entity {

  private double eW = 0.0;
  private double eQ = 0.0;

  @Getter @Setter private BsStateType state = BsStateType.OFF;
  @Getter private BsStateType nextStateBs = BsStateType.OFF;
  @Getter private long idEventNextState;
  private long wCounter = 0;
  private double qAux = 0.0;
  private double tAux = 0.0;

  public Bs(final ClientCommunicator communicator, final Position position) {
    super(communicator);
    this.position = position;
  }

  public final double getEw() {
    return wCounter != 0 ? (eW / wCounter) : 0;
  }

  public final double getEq() {
    return tAux != 0 ? (eQ / tAux) : 0;
  }

  public final void setNextStateBs(final BsStateType nextStateBs, final long idEventNextState) {
    this.nextStateBs = nextStateBs;
    this.idEventNextState = idEventNextState;
  }

  public final void addW(final double w) {
    this.eW += w;
    this.wCounter++;
  }

  public final void addQ(final double q, final double t) {
    this.eQ += qAux * (t - tAux);
    this.qAux = q;
    this.tAux = t;
  }
}
