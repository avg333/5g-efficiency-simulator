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
public class TrafficArrivalResponseDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.TRAFFIC_ARRIVAL_RESPONSE;

  private final BsStateType state;

  private final BsStateType nextState;

  private final double q;

  private final boolean isTrafficEgress;

  private final double tTrafficEgress;

  private final boolean isNewState;

  private final double tNewState;

  private final double a;

  public TrafficArrivalResponseDto(
      final BsStateType state,
      final BsStateType nextState,
      final double q,
      final double tTrafficEgress,
      final double tNewState,
      final double a) {
    super(IDENTIFIER);
    this.state = state;
    this.nextState = nextState;
    this.q = q;
    this.isTrafficEgress = NO_TASK_TO_PROCESS.getValue() != tTrafficEgress;
    this.tTrafficEgress = this.isTrafficEgress ? tTrafficEgress : NO_TASK_TO_PROCESS.getValue();
    this.isNewState = NO_NEXT_STATE.getValue() != tNewState;
    this.tNewState = this.isNewState ? tNewState : NO_NEXT_STATE.getValue();
    this.a = a;
  }

  public TrafficArrivalResponseDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(
        BsStateType.fromValue(messageUnpacker.unpackByte()),
        BsStateType.fromValue(messageUnpacker.unpackByte()),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackBoolean()
            ? messageUnpacker.unpackDouble()
            : NO_TASK_TO_PROCESS.getValue(),
        messageUnpacker.unpackBoolean() ? messageUnpacker.unpackDouble() : NO_NEXT_STATE.getValue(),
        messageUnpacker.unpackDouble());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packByte(state.getValue());
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
    messageBufferPacker.packDouble(a);
  }
}
