package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import types.BsStateType;

@Getter
public class NewStateResponseDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.NEW_STATE_RESPONSE;

  private final double q;

  private final BsStateType stateReceived;

  private final double tTrafficEgress;

  private final double tNewState;

  private final BsStateType nextState;

  public NewStateResponseDto(
      final double q,
      final BsStateType stateReceived,
      final double tTrafficEgress,
      final double tNewState,
      final BsStateType nextState) {
    super(IDENTIFIER);
    this.q = q;
    this.stateReceived = stateReceived;
    this.tTrafficEgress = tTrafficEgress;
    this.tNewState = tNewState;
    this.nextState = nextState;
  }

  public NewStateResponseDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(
        messageUnpacker.unpackDouble(),
        BsStateType.fromCode(messageUnpacker.unpackByte()),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackDouble(),
        BsStateType.fromCode(messageUnpacker.unpackByte()));
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(q);
    messageBufferPacker.packByte(stateReceived.getValue());
    messageBufferPacker.packDouble(tTrafficEgress);
    messageBufferPacker.packDouble(tNewState);
    messageBufferPacker.packByte(nextState.getValue());
  }
}
