package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

@Getter
public class TrafficIngressResponseDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.TRAFFIC_INGRESS_RESPONSE;

  private final double x;
  private final double y;
  private final double size;
  private final double tUntilNextTask;

  public TrafficIngressResponseDto(
      final double x, final double y, final double size, final double tUntilNextTask) {
    super(IDENTIFIER);
    this.x = x;
    this.y = y;
    this.size = size;
    this.tUntilNextTask = tUntilNextTask;
  }

  public TrafficIngressResponseDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackDouble());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(x);
    messageBufferPacker.packDouble(y);
    messageBufferPacker.packDouble(size);
    messageBufferPacker.packDouble(tUntilNextTask);
  }
}
