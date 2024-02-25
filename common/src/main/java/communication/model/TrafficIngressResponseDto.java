package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import domain.Position;
import domain.Task;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;

@Getter
public class TrafficIngressResponseDto extends Dto {

  private final Position position;

  private final Task task;

  public TrafficIngressResponseDto(Position position, Task task) {
    super(DtoIdentifier.TRAFFIC_INGRESS_RESPONSE);
    this.position = position;
    this.task = task;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(position.getX());
    messageBufferPacker.packDouble(position.getY());
    messageBufferPacker.packDouble(task.size());
    messageBufferPacker.packDouble(task.tArrive());
  }
}
