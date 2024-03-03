package loggers.model;

import static domain.EventType.TRAFFIC_EGRESS;
import static types.EntityType.BASE_STATION;

import domain.entities.Bs;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrafficEgressDtoLog implements BaseDtoLog {

  private static final String LOG_LINE =
      "%.2f BS %d TRAFFIC_EGRESS id=%d size=%.2f q=%.2f wait=%.2f";

  private final double t;
  private final Bs bs;
  private final long taskId;
  private final double taskSize;
  private final double q;
  private final double w;

  @Override
  public final String getLogLine() {
    return String.format(LOG_LINE, t, bs.getId(), taskId, taskSize, q, w);
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
