package loggers.model;

import static domain.EventType.TRAFFIC_ROUTE;
import static types.EntityType.BROKER;

import domain.Task;
import domain.entities.Bs;
import domain.entities.Ue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrafficRouteDtoLog implements BaseDtoLog {

  private final double t;
  private final Ue ue;
  private final Bs bs;
  private final Task task;

  @Override
  public final String getLogLine() {
    return t
        + " BK 0 TRAFFIC_ROUTE id="
        + task.id()
        + " size="
        + task.size()
        + " from-ue="
        + ue.getId()
        + " to-bs="
        + bs.getId();
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
