package broker;

import static communication.model.base.DtoIdentifier.NEW_STATE_RESPONSE;
import static communication.model.base.DtoIdentifier.TRAFFIC_ARRIVAL_RESPONSE;
import static communication.model.base.DtoIdentifier.TRAFFIC_EGRESS_RESPONSE;
import static communication.model.base.DtoIdentifier.TRAFFIC_INGRESS_RESPONSE;
import static domain.Event.createNewEvent;
import static domain.EventType.NEW_STATE;
import static domain.EventType.TRAFFIC_EGRESS;
import static domain.Task.createNewTask;
import static types.Constants.NO_NEXT_STATE;
import static types.Constants.NO_TASK_TO_PROCESS;

import communication.RegisterServer;
import communication.model.NewStateRequestDto;
import communication.model.NewStateResponseDto;
import communication.model.TrafficArrivalRequestDto;
import communication.model.TrafficArrivalResponseDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficEgressResponseDto;
import communication.model.TrafficIngressRequestDto;
import communication.model.TrafficIngressResponseDto;
import domain.Event;
import domain.EventType;
import domain.Position;
import domain.Task;
import domain.entities.Bs;
import domain.entities.Entity;
import domain.entities.Ue;
import exception.InvalidEventTypeException;
import java.util.*;
import loggers.LoggerCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import routing.BsRouter;
import types.BsStateType;

@Slf4j
@RequiredArgsConstructor
public class Broker implements Runnable {

  private final RegisterServer server;
  private final BsRouter bsRouter;
  private final double tFinal;
  private final LoggerCustom loggerCustom;
  private final PriorityQueue<Event> eventQueue =
      new PriorityQueue<>(Comparator.comparing(Event::t));
  private List<Bs> bsList;
  private List<Ue> ueList;
  private double t = 0;

  public static void main(String[] args) {
    new Thread(new BrokerFactory().createBroker()).start();
  }

  @Override
  public void run() {
    processEntities(server.getEntities());

    final long start = System.currentTimeMillis();
    try (final ProgressBar pb = new ProgressBar("Simulating", (long) tFinal)) {
      while (t <= tFinal) {
        final Event event = eventQueue.poll();
        if (event == null) {
          log.warn("Event queue is empty. Simulation finished at t={}", t);
          break;
        }
        processEvent(event);
        pb.stepTo((long) t);
      }
      pb.stepTo((long) tFinal);
    }
    final long finish = System.currentTimeMillis();

    server.closeSockets();
    loggerCustom.printResults(finish - start, t, bsList, ueList);
  }

  private void processEntities(final List<Entity> entities) {
    bsList = entities.stream().filter(Bs.class::isInstance).map(Bs.class::cast).toList();
    ueList = entities.stream().filter(Ue.class::isInstance).map(Ue.class::cast).toList();
    bsList.forEach(bs -> eventQueue.add(createNewEvent(t, NEW_STATE, bs)));
    ueList.forEach(ue -> eventQueue.add(createNewEvent(t, EventType.TRAFFIC_INGRESS, ue)));
  }

  private void processEvent(final Event event) {
    t = event.t();
    final EventType type = event.type();

    switch (type) {
      case TRAFFIC_INGRESS -> processTrafficIngress(event);
      case TRAFFIC_EGRESS -> processTrafficEgress(event);
      case NEW_STATE -> processNewState(event);
      default -> {
        log.error("Type {} not supported. Execution completed", type);
        server.closeSockets();
        throw new InvalidEventTypeException(type);
      }
    }
  }

  /*
   * Process the traffic ingress event, sending the task to the best BS and logging the event.
   * After that, creates traffic arrive event for chosen BS.
   */
  private void processTrafficIngress(final Event event) {
    final Ue ue = (Ue) event.entity();

    final TrafficIngressResponseDto dto =
        (TrafficIngressResponseDto)
            ue.communicate(new TrafficIngressRequestDto(), TRAFFIC_INGRESS_RESPONSE.getSize());

    final Task task = createNewTask(dto.getSize(), t, dto.getTUntilNextTask());

    eventQueue.add(createNewEvent(t + task.tUntilNextTask(), EventType.TRAFFIC_INGRESS, ue));

    if (task.isEmpty()) {
      return;
    }

    ue.addTask(new Position(dto.getX(), dto.getY()), task);

    loggerCustom.logTrafficIngress(t, ue, task);

    final Bs bs = processTrafficRoute(ue, task);

    processTrafficArrive(task, bs);
  }

  /*
   * Choose the best BS to send the task from the UE, returning the chosen BS and logging the event.
   */
  protected Bs processTrafficRoute(final Ue ue, final Task task) {
    final Bs bs = bsRouter.getBs(ue, bsList);
    loggerCustom.logTrafficRoute(t, ue, bs, task);
    return bs;
  }

  private void processTrafficArrive(final Task task, final Bs bs) {
    final TrafficArrivalResponseDto responseTA =
        (TrafficArrivalResponseDto)
            bs.communicate(
                new TrafficArrivalRequestDto(task.id(), task.size(), task.tArrivalTime()),
                TRAFFIC_ARRIVAL_RESPONSE.getSize());

    final BsStateType state = responseTA.getState();
    final BsStateType nextState = responseTA.getNextState();
    final double q = responseTA.getQ();
    final double tTrafficEgress = responseTA.getTTrafficEgress();
    final double tNewState = responseTA.getTNewState();
    final double a = responseTA.getA();

    loggerCustom.logTrafficArrival(t, bs, task, q, a);

    if (bs.getState() == BsStateType.HYSTERESIS) {
      // How often does this happen?
      loggerCustom.logNewState(t, bs, q, state);
      eventQueue.removeIf(event -> event.id() == bs.getIdEventNextState());
    } else if (state != bs.getState()) {
      loggerCustom.logNewState(t, bs, q, state);
    }

    createNewStateEventIfNecessary(bs, tNewState, nextState);
    createTrafficEgressEventIfNecessary(bs, tTrafficEgress);

    bs.addQ(q, t);
    bs.setState(state);
  }

  private void processTrafficEgress(final Event event) {
    final Bs bs = (Bs) event.entity();

    final TrafficEgressResponseDto responseTE =
        (TrafficEgressResponseDto)
            bs.communicate(new TrafficEgressRequestDto(t), TRAFFIC_EGRESS_RESPONSE.getSize());

    final BsStateType state = responseTE.getState();
    final BsStateType nextState = responseTE.getNextState();
    final double q = responseTE.getQ();
    final double tTrafficEgress = responseTE.getTTrafficEgress();
    final double tNewState = responseTE.getTNewState();
    final long id = responseTE.getId();
    final double size = responseTE.getSize();
    final double w = responseTE.getW();

    loggerCustom.logTrafficEgress(t, bs, id, size, q, w);

    if (state != bs.getState()) {
      loggerCustom.logNewState(t, bs, q, state);
    }

    createNewStateEventIfNecessary(bs, tNewState, nextState);
    createTrafficEgressEventIfNecessary(bs, tTrafficEgress);

    bs.addQ(q, t);
    bs.addW(w);
    bs.setState(state);
  }

  private void processNewState(final Event event) {
    final Bs bs = (Bs) event.entity();

    final NewStateResponseDto responseNS =
        (NewStateResponseDto)
            bs.communicate(
                new NewStateRequestDto(bs.getNextStateBs()), NEW_STATE_RESPONSE.getSize());

    final BsStateType state = responseNS.getStateReceived();
    final BsStateType nextState = responseNS.getNextState();
    final double q = responseNS.getQ();
    final double tTrafficEgress = responseNS.getTTrafficEgress();
    final double tNewState = responseNS.getTNewState();

    if (state != bs.getState()) {
      loggerCustom.logNewState(t, bs, q, state);
    }

    createNewStateEventIfNecessary(bs, tNewState, nextState);
    createTrafficEgressEventIfNecessary(bs, tTrafficEgress);

    bs.setState(state);
  }

  private void createNewStateEventIfNecessary(
      final Bs bs, final double tNewState, final BsStateType nextState) {
    if (tNewState != NO_NEXT_STATE.getValue()) {
      final Event newState = createNewEvent(t + tNewState, NEW_STATE, bs);
      eventQueue.add(newState);
      bs.setNextStateBs(nextState);
      bs.setIdEventNextState(newState.id());
    }
  }

  private void createTrafficEgressEventIfNecessary(final Bs bs, final double tTrafficEgress) {
    if (tTrafficEgress != NO_TASK_TO_PROCESS.getValue()) {
      eventQueue.add(createNewEvent(t + tTrafficEgress, TRAFFIC_EGRESS, bs));
    }
  }
}
