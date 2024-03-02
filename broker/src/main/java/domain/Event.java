package domain;

import domain.entities.Entity;

public record Event(long id, double t, EventType type, Entity entity) {
  private static long eventCounter = 0;

  public static Event createNewEvent(double t, EventType type, Entity entity) {
    return new Event(eventCounter++, t, type, entity);
  }
}
