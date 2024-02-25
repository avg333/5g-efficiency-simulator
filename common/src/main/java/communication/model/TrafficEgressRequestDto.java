package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;

@Getter
public class TrafficEgressRequestDto extends Dto {

  private final double t;

  public TrafficEgressRequestDto(double t) {
    super(DtoIdentifier.TRAFFIC_EGRESS_REQUEST);
    this.t = t;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(t);
  }
}
