package loggers.model;

import static domain.EventType.NEW_STATE;

import domain.entities.Bs;
import lombok.RequiredArgsConstructor;
import types.BsStateType;
import types.EntityType;

@RequiredArgsConstructor
public class NewStateDtoLog implements BaseDtoLog {

  private static final String LOG_LINE = "%.2f BS %d NEW_STATE q=%.2f state=%s";

  private final double t;
  private final Bs bs;
  private final double q;
  private final BsStateType state;

  @Override
  public final String getLogLine() {
    return String.format(LOG_LINE, t, bs.getId(), q, state);
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
