package broker;

import static domain.Event.createNewEvent;
import static domain.EventType.NEW_STATE;

import domain.Event;
import domain.EventType;
import domain.entities.Bs;
import domain.entities.Entity;
import domain.entities.Ue;
import exception.NoEventsAvailableException;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrokerState {

  private final PriorityQueue<Event> eventQueue =
      new PriorityQueue<>(Comparator.comparing(Event::t));
  @Getter private final List<Bs> bsList;
  @Getter private final List<Ue> ueList;
  @Getter private double t = 0;

  public BrokerState(final List<Entity> entities) {
    bsList = entities.stream().filter(Bs.class::isInstance).map(Bs.class::cast).toList();
    ueList = entities.stream().filter(Ue.class::isInstance).map(Ue.class::cast).toList();
    bsList.forEach(bs -> eventQueue.add(createNewEvent(t, NEW_STATE, bs)));
    ueList.forEach(ue -> eventQueue.add(createNewEvent(t, EventType.TRAFFIC_INGRESS, ue)));
  }

  public Event pollNextElement() {
    final Event event = eventQueue.poll();
    if (event == null) {
      log.error("No events available. Simulation finished at t={}", t);
      throw new NoEventsAvailableException();
    }
    t = event.t();
    return event;
  }

  public void addEvent(final Event event) {
    eventQueue.add(event);
  }

  public void removeEventById(final long id) {
    log.debug("Removing event with id {}", id);
    eventQueue.removeIf(event -> event.id() == id);
  }
}
