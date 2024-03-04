package loggers.model;

import static domain.EventType.TRAFFIC_ARRIVE;
import static types.EntityType.BASE_STATION;

import domain.Task;
import domain.entities.Bs;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrafficArrivalDtoLog implements BaseDtoLog {

  private final double t;
  private final Bs bs;
  private final Task task;
  private final double q;
  private final double a;

  @Override
  public final String getLogLine() {
    return t
        + " BS "
        + bs.getId()
        + " TRAFFIC_ARRIVAL id="
        + task.id()
        + " size="
        + task.size()
        + " a="
        + a
        + " q="
        + q;
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
