package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import types.BsStateType;

@Getter
public class TrafficArrivalResponseDto extends Dto {

  private final double q;

  private final BsStateType state;

  private final double tTrafficEgress;

  private final double tNewState;

  private final BsStateType nextState;

  private final double a;

  public TrafficArrivalResponseDto(
      double q,
      BsStateType state,
      double tTrafficEgress,
      double tNewState,
      BsStateType nextState,
      double a) {
    super(DtoIdentifier.TRAFFIC_ARRIVAL_RESPONSE);
    this.q = q;
    this.state = state;
    this.tTrafficEgress = tTrafficEgress;
    this.tNewState = tNewState;
    this.nextState = nextState;
    this.a = a;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(q);
    messageBufferPacker.packByte(state.getValue());
    messageBufferPacker.packDouble(tTrafficEgress);
    messageBufferPacker.packDouble(tNewState);
    messageBufferPacker.packByte(nextState.getValue());
    messageBufferPacker.packDouble(a);
  }
}
