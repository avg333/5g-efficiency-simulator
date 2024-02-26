package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;

@Getter
public class TrafficIngressResponseDto extends Dto {

  private final double x;
  private final double y;
  private final double size;
  private final double tUntilNextTask;

  public TrafficIngressResponseDto(double x, double y, double size, double tUntilNextTask) {
    super(DtoIdentifier.TRAFFIC_INGRESS_RESPONSE);
    this.x = x;
    this.y = y;
    this.size = size;
    this.tUntilNextTask = tUntilNextTask;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(x);
    messageBufferPacker.packDouble(y);
    messageBufferPacker.packDouble(size);
    messageBufferPacker.packDouble(tUntilNextTask);
  }
}
