package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;

@Getter
public class TrafficArrivalRequestDto extends Dto {

  private final long taskId;
  private final double taskSize;
  private final double taskTArrivalTime;

  public TrafficArrivalRequestDto(long taskId, double taskSize, double taskTArrivalTime) {
    super(DtoIdentifier.TRAFFIC_ARRIVAL_REQUEST);
    this.taskId = taskId;
    this.taskSize = taskSize;
    this.taskTArrivalTime = taskTArrivalTime;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packLong(taskId);
    messageBufferPacker.packDouble(taskSize);
    messageBufferPacker.packDouble(taskTArrivalTime);
  }
}
