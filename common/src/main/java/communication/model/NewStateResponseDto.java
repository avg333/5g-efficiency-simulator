package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import types.BsStateType;

@Getter
public class NewStateResponseDto extends Dto {

  private final double q;

  private final BsStateType stateReceived;

  private final double tTrafficEgress;

  private final double tNewState;

  private final BsStateType nextState;

  public NewStateResponseDto(
      double q,
      BsStateType stateReceived,
      double tTrafficEgress,
      double tNewState,
      BsStateType nextState) {
    super(DtoIdentifier.NEW_STATE_RESPONSE);
    this.q = q;
    this.stateReceived = stateReceived;
    this.tTrafficEgress = tTrafficEgress;
    this.tNewState = tNewState;
    this.nextState = nextState;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(q);
    messageBufferPacker.packByte(stateReceived.getValue());
    messageBufferPacker.packDouble(tTrafficEgress);
    messageBufferPacker.packDouble(tNewState);
    messageBufferPacker.packByte(nextState.getValue());
  }
}
