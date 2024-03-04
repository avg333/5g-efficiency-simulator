package loggers.model;

import static domain.EventType.TRAFFIC_EGRESS;
import static types.EntityType.BASE_STATION;

import domain.entities.Bs;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrafficEgressDtoLog implements BaseDtoLog {

  private final double t;
  private final Bs bs;
  private final long taskId;
  private final double taskSize;
  private final double q;
  private final double w;

  @Override
  public final String getLogLine() {
    return t
        + " BS "
        + bs.getId()
        + " TRAFFIC_EGRESS id="
        + taskId
        + " size="
        + taskSize
        + " q="
        + q
        + " wait="
        + w;
  }

  @Override
  public final BaseCsvDtoLog toCsvDtoLog() {

    return new BaseCsvDtoLog(
        t,
        BASE_STATION,
        bs.getId(),
        TRAFFIC_EGRESS,
        taskId,
        taskSize,
        null,
        null,
        null,
        null,
        null,
        q,
        w,
        null);
  }
}
