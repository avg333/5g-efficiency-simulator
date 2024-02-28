package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

@Getter
public class TrafficEgressRequestDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.TRAFFIC_EGRESS_REQUEST;

  private final double t;

  public TrafficEgressRequestDto(final double t) {
    super(IDENTIFIER);
    this.t = t;
  }

  public TrafficEgressRequestDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(messageUnpacker.unpackDouble());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(t);
  }
}
