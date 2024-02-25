package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import domain.Position;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;

@Getter
public class TrafficIngressResponseDto extends Dto {

  private final Position position;

  private final double size;

  private final double tUntilNextTask;

  public TrafficIngressResponseDto(Position position, double size, double tUntilNextTask) {
    super(DtoIdentifier.TRAFFIC_INGRESS_RESPONSE);
    this.position = position;
    this.size = size;
    this.tUntilNextTask = tUntilNextTask;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(position.getX());
    messageBufferPacker.packDouble(position.getY());
    messageBufferPacker.packDouble(size);
    messageBufferPacker.packDouble(tUntilNextTask);
  }
}
