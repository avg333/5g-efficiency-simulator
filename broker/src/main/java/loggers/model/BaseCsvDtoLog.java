package loggers.model;

import domain.EventType;
import types.BsStateType;
import types.EntityType;

public record BaseCsvDtoLog(
    Double t,
    EntityType entityType,
    Integer entityId,
    EventType eventType,
    Long taskId,
    Double taskSize,
    Double a,
    Double x,
    Double y,
    Integer ueId,
    Integer bsId,
    Double q,
    Double taskWait,
    BsStateType state) {

  public static final String[] COLUMNS = {
    "T", "ENTITY", "ID", "EVENT", "TASK", "L", "A", "X", "Y", "FROM-UE", "TO-BS", "Q", "W", "STATE"
  };

  public Object[] getValues() {
    return new Object[] {
      t, entityType, entityId, eventType, taskId, taskSize, a, x, y, ueId, bsId, q, taskWait, state
    };
  }
}
