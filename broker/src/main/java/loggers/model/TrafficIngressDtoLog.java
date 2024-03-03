package loggers.model;

import static domain.EventType.TRAFFIC_INGRESS;
import static types.EntityType.USER_EQUIPMENT;

import domain.Task;
import domain.entities.Ue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrafficIngressDtoLog implements BaseDtoLog {

  private static final String LOG_LINE =
      "%.2f entity=USER_EQUIPMENT %d event=TRAFFIC_INGRESS id=%d size=%.2f next=%.2f x=%.2f y=%.2f";

  private final double t;
  private final Ue ue;
  private final Task task;

  @Override
  public final String getLogLine() {
    return String.format(
        LOG_LINE,
        t,
        ue.getId(),
        task.id(),
        task.size(),
        task.tUntilNextTask(),
        ue.getPosition().x(),
        ue.getPosition().y());
  }

  @Override
  public final BaseCsvDtoLog toCsvDtoLog() {
    return new BaseCsvDtoLog(
        t,
        USER_EQUIPMENT,
        ue.getId(),
        TRAFFIC_INGRESS,
        task.id(),
        task.size(),
        task.tUntilNextTask(),
        ue.getPosition().x(),
        ue.getPosition().y(),
        null,
        null,
        null,
        null,
        null);
  }
}
