package communication.model;

import static types.Constants.NO_NEXT_STATE;
import static types.Constants.NO_TASK_TO_PROCESS;

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

  private final BsStateType stateReceived;

  private final BsStateType nextState;

  private final double q;

  private final boolean isTrafficEgress;

  private final double tTrafficEgress;

  private final boolean isNewState;

  private final double tNewState;

  public NewStateResponseDto(
      final BsStateType stateReceived,
      final BsStateType nextState,
      final double q,
      final double tTrafficEgress,
      final double tNewState) {
    super(IDENTIFIER);
    this.stateReceived = stateReceived;
    this.nextState = nextState;
    this.q = q;
    this.isTrafficEgress = NO_TASK_TO_PROCESS.getValue() != tTrafficEgress;
    this.tTrafficEgress = this.isTrafficEgress ? tTrafficEgress : NO_TASK_TO_PROCESS.getValue();
    this.isNewState = NO_NEXT_STATE.getValue() != tNewState;
    this.tNewState = this.isNewState ? tNewState : NO_NEXT_STATE.getValue();
  }

  public NewStateResponseDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(
        BsStateType.fromCode(messageUnpacker.unpackByte()),
        BsStateType.fromCode(messageUnpacker.unpackByte()),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackBoolean()
            ? messageUnpacker.unpackDouble()
            : NO_TASK_TO_PROCESS.getValue(),
        messageUnpacker.unpackBoolean()
            ? messageUnpacker.unpackDouble()
            : NO_NEXT_STATE.getValue());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packByte(stateReceived.getValue());
    messageBufferPacker.packByte(nextState.getValue());
    messageBufferPacker.packDouble(q);
    messageBufferPacker.packBoolean(isTrafficEgress);
    if (isTrafficEgress) {
      messageBufferPacker.packDouble(tTrafficEgress);
    }
    messageBufferPacker.packBoolean(isNewState);
    if (isNewState) {
      messageBufferPacker.packDouble(tNewState);
    }
  }
}
