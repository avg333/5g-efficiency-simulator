package loggers.model;

import static domain.EventType.TRAFFIC_INGRESS;
import static types.EntityType.USER_EQUIPMENT;

import domain.Task;
import domain.entities.Ue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrafficIngressDtoLog implements BaseDtoLog {

  private final double t;
  private final Ue ue;
  private final Task task;

  @Override
  public final String getLogLine() {
    return t
        + " entity=USER_EQUIPMENT "
        + ue.getId()
        + " event=TRAFFIC_INGRESS id="
        + task.id()
        + " size="
        + task.size()
        + " next="
        + task.tUntilNextTask()
        + " x="
        + ue.getPosition().x()
        + " y="
        + ue.getPosition().y();
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
