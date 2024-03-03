package loggers.model;

import static domain.EventType.TRAFFIC_ARRIVE;
import static types.EntityType.BASE_STATION;

import domain.Task;
import domain.entities.Bs;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrafficArrivalDtoLog implements BaseDtoLog {

  private static final String LOG_LINE = "%.2f BS %d TRAFFIC_ARRIVAL id=%d size=%.2f a=%.2f q=%.2f";

  private final double t;
  private final Bs bs;
  private final Task task;
  private final double q;
  private final double a;

  @Override
  public final String getLogLine() {
    return String.format(LOG_LINE, t, bs.getId(), task.id(), task.size(), a, q);
  }

  @Override
  public final BaseCsvDtoLog toCsvDtoLog() {
    return new BaseCsvDtoLog(
        t,
        BASE_STATION,
        bs.getId(),
        TRAFFIC_ARRIVE,
        task.id(),
        task.size(),
        a,
        null,
        null,
        null,
        null,
        q,
        null,
        null);
  }
}
