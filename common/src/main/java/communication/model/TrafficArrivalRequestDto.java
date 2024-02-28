package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import lombok.ToString;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

@Getter
@ToString
public class TrafficArrivalRequestDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.TRAFFIC_ARRIVAL_REQUEST;

  private final long taskId;
  private final double taskSize;
  private final double taskTArrivalTime;

  public TrafficArrivalRequestDto(
      final long taskId, final double taskSize, final double taskTArrivalTime) {
    super(IDENTIFIER);
    this.taskId = taskId;
    this.taskSize = taskSize;
    this.taskTArrivalTime = taskTArrivalTime;
  }

  public TrafficArrivalRequestDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(
        messageUnpacker.unpackLong(),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackDouble());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packLong(taskId);
    messageBufferPacker.packDouble(taskSize);
    messageBufferPacker.packDouble(taskTArrivalTime);
  }
}
