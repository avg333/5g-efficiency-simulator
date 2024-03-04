package loggers.model;

import static domain.EventType.NEW_STATE;

import domain.entities.Bs;
import lombok.RequiredArgsConstructor;
import types.BsStateType;
import types.EntityType;

@RequiredArgsConstructor
public class NewStateDtoLog implements BaseDtoLog {

  private final double t;
  private final Bs bs;
  private final double q;
  private final BsStateType state;

  @Override
  public final String getLogLine() {
    return t + " BS " + bs.getId() + " NEW_STATE q=" + q + " state=" + state;
  }

  @Override
  public final BaseCsvDtoLog toCsvDtoLog() {

    return new BaseCsvDtoLog(
        t,
        EntityType.BASE_STATION,
        bs.getId(),
        NEW_STATE,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        q,
        null,
        state);
  }
}
