package loggers.model;

import static domain.EventType.TRAFFIC_ROUTE;
import static types.EntityType.BROKER;

import domain.Task;
import domain.entities.Bs;
import domain.entities.Ue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrafficRouteDtoLog implements BaseDtoLog {

  private static final String LOG_LINE =
      "%.2f BK 0 TRAFFIC_ROUTE id=%d size=%.2f from-ue=%d to-bs=%d";

  private final double t;
  private final Ue ue;
  private final Bs bs;
  private final Task task;

  @Override
  public final String getLogLine() {
    return String.format(LOG_LINE, t, task.id(), task.size(), ue.getId(), bs.getId());
  }

  @Override
  public final BaseCsvDtoLog toCsvDtoLog() {
    return new BaseCsvDtoLog(
        t,
        BROKER,
        0,
        TRAFFIC_ROUTE,
        task.id(),
        task.size(),
        null,
        null,
        null,
        ue.getId(),
        bs.getId(),
        null,
        null,
        null);
  }
}
