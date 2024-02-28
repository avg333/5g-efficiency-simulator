package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import types.BsStateType;

@Getter
public class TrafficEgressResponseDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.TRAFFIC_EGRESS_RESPONSE;

  private final double q;

  private final BsStateType state;

  private final double tTrafficEgress;

  private final double tNewState;

  private final BsStateType nextState;

  private final double w;

  private final long id;

  private final double size;

  public TrafficEgressResponseDto(
      final double q,
      final BsStateType state,
      final double tTrafficEgress,
      final double tNewState,
      final BsStateType nextState,
      final double w,
      final long id,
      final double size) {
    super(IDENTIFIER);
    this.q = q;
    this.state = state;
    this.tTrafficEgress = tTrafficEgress;
    this.tNewState = tNewState;
    this.nextState = nextState;
    this.w = w;
    this.id = id;
    this.size = size;
  }

  public TrafficEgressResponseDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(
        messageUnpacker.unpackDouble(),
        BsStateType.getStateTypeByCode(messageUnpacker.unpackByte()),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackDouble(),
        BsStateType.getStateTypeByCode(messageUnpacker.unpackByte()),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackLong(),
        messageUnpacker.unpackDouble());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(q);
    messageBufferPacker.packByte(state.getValue());
    messageBufferPacker.packDouble(tTrafficEgress);
    messageBufferPacker.packDouble(tNewState);
    messageBufferPacker.packByte(nextState.getValue());
    messageBufferPacker.packDouble(w);
    messageBufferPacker.packLong(id);
    messageBufferPacker.packDouble(size);
  }
}
