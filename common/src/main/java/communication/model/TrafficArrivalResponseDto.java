package communication.model;

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

  private final double q;

  private final BsStateType state;

  private final double tTrafficEgress;

  private final double tNewState;

  private final BsStateType nextState;

  private final double a;

  public TrafficArrivalResponseDto(
      final double q,
      final BsStateType state,
      final double tTrafficEgress,
      final double tNewState,
      final BsStateType nextState,
      final double a) {
    super(IDENTIFIER);
    this.q = q;
    this.state = state;
    this.tTrafficEgress = tTrafficEgress;
    this.tNewState = tNewState;
    this.nextState = nextState;
    this.a = a;
  }

  public TrafficArrivalResponseDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(
        messageUnpacker.unpackDouble(),
        BsStateType.fromCode(messageUnpacker.unpackByte()),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackDouble(),
        BsStateType.fromCode(messageUnpacker.unpackByte()),
        messageUnpacker.unpackDouble());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(q);
    messageBufferPacker.packByte(state.getValue());
    messageBufferPacker.packDouble(tTrafficEgress);
    messageBufferPacker.packDouble(tNewState);
    messageBufferPacker.packByte(nextState.getValue());
    messageBufferPacker.packDouble(a);
  }
}
