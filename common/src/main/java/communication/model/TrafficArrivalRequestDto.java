package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import domain.Task;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;

@Getter
public class TrafficArrivalRequestDto extends Dto {

  private final Task task;

  public TrafficArrivalRequestDto(Task task) {
    super(DtoIdentifier.TRAFFIC_ARRIVAL_REQUEST);
    this.task = task;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packDouble(task.tArrive());
    messageBufferPacker.packLong(task.id());
    messageBufferPacker.packDouble(task.size());
  }
}
