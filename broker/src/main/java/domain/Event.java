package domain;

import domain.entities.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Event {

  private static long totalEvents = 0;

  private final long id = totalEvents++;
  private final double t;
  private final EventType type;
  private final Entity entity;
}
