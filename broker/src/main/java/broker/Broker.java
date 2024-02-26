package broker;

import static communication.model.base.DtoIdentifier.NEW_STATE_RESPONSE;
import static communication.model.base.DtoIdentifier.TRAFFIC_ARRIVAL_RESPONSE;
import static communication.model.base.DtoIdentifier.TRAFFIC_EGRESS_RESPONSE;
import static communication.model.base.DtoIdentifier.TRAFFIC_INGRESS_RESPONSE;

import communication.RegisterServer;
import communication.RegisterServerTCP;
import communication.RegisterServerUDP;
import communication.model.NewStateRequestDto;
import communication.model.NewStateResponseDto;
import communication.model.TrafficArrivalRequestDto;
import communication.model.TrafficArrivalResponseDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficEgressResponseDto;
import communication.model.TrafficIngressRequestDto;
import communication.model.TrafficIngressResponseDto;
import domain.Position;
import domain.Task;
import entities.Bs;
import entities.Entity;
import entities.Ue;
import java.util.*;
import loggers.LoggerCustom;
import lombok.extern.slf4j.Slf4j;
import routing.BsRouter;
import types.BsStateType;

@Slf4j
public class Broker implements Runnable {

  private final RegisterServer server;
  private final BsRouter bsRouter;
  private final double tFinal;
  private final PriorityQueue<Event> eventQueue =
      new PriorityQueue<>(Comparator.comparing(Event::getT));
  private final LoggerCustom loggerCustom;
  private List<Bs> bsList;
  private List<Ue> ueList;
  private double t = 0;
  private long taskCounter = 0;

  public Broker(
      int port, boolean communicatorModeTCP, boolean eventsLog, BsRouter bsRouter, double tFinal) {
    this.bsRouter = bsRouter;
    this.tFinal = tFinal;
    this.loggerCustom = new LoggerCustom(eventsLog);

    server = (communicatorModeTCP) ? new RegisterServerTCP(port) : new RegisterServerUDP(port);

    log.info(
        "Started in [port={}] with {}, simulator time [t={}] and {}",
        port,
        bsRouter,
        tFinal,
        loggerCustom);
    log.info("Press enter to start the simulation");
  }

  public static void main(String[] args) {
    new Thread(new BrokerFactory().createBroker()).start();
  }

  @Override
  public void run() {
    processEntities(server.getEntities());

    final long start = System.currentTimeMillis();
    while (t <= tFinal) {
      final Event event = eventQueue.poll();
      processEvent(event);
      loggerCustom.printProgress(t, tFinal);
    }
    final long finish = System.currentTimeMillis();

    server.closeSockets();
    loggerCustom.printResults(finish - start, t, bsList, ueList);
  }

  private void processEntities(final List<Entity> entities) {
    bsList = entities.stream().filter(Bs.class::isInstance).map(Bs.class::cast).toList();
    ueList = entities.stream().filter(Ue.class::isInstance).map(Ue.class::cast).toList();
    bsList.forEach(bs -> eventQueue.add(new Event(t, EventType.NEW_STATE, bs)));
    ueList.forEach(ue -> eventQueue.add(new Event(t, EventType.TRAFFIC_INGRESS, ue)));
  }

  private void processEvent(final Event event) {
    t = event.getT();
    final EventType type = event.getType();

    switch (type) {
      case TRAFFIC_INGRESS -> processTrafficIngress(event);
      case TRAFFIC_EGRESS -> processTrafficEgress(event);
      case NEW_STATE -> processNewState(event);
      default -> {
        log.error("Type {} not supported. Execution completed", type);
        System.exit(-1);
      }
    }
  }

  /*
   * Process the traffic ingress event, sending the task to the best BS and logging the event.
   * After that, creates traffic arrive event for chosen BS.
   */
  private void processTrafficIngress(final Event event) {
    final Ue ue = (Ue) event.getEntity();

    final TrafficIngressResponseDto dto =
        (TrafficIngressResponseDto)
            ue.communicate(new TrafficIngressRequestDto(), TRAFFIC_INGRESS_RESPONSE.getSize());

    final Task task = new Task(taskCounter++, dto.getSize(), t, dto.getTUntilNextTask());
    final Position position = dto.getPosition();

    eventQueue.add(new Event(t + task.tUntilNextTask(), EventType.TRAFFIC_INGRESS, ue));

    if (task.isEmpty()) {
      return;
    }

    ue.addTask(position, task);

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
            bs.communicate(new TrafficArrivalRequestDto(task), TRAFFIC_ARRIVAL_RESPONSE.getSize());

    final double q = responseTA.getQ();
    final BsStateType state = responseTA.getState();
    final double tTrafficEgress = responseTA.getTTrafficEgress();
    final double tNewState = responseTA.getTNewState();
    final BsStateType nextState = responseTA.getNextState();
    final double a = responseTA.getA();

    loggerCustom.logTrafficArrival(t, bs, task, q, a);

    if (bs.getState() == BsStateType.HYSTERESIS) {
      loggerCustom.logNewState(t, bs, q, state);
      eventQueue.removeIf(event -> event.getId() == bs.getIdEventNextState());
    } else if (state != bs.getState()) {
      loggerCustom.logNewState(t, bs, q, state);
    }

    createEvents(bs, tNewState, tTrafficEgress, nextState);

    bs.addQ(q, t);
    bs.setState(state);
  }

  private void processTrafficEgress(final Event event) {
    final Bs bs = (Bs) event.getEntity();

    final TrafficEgressResponseDto responseTE =
        (TrafficEgressResponseDto)
            bs.communicate(new TrafficEgressRequestDto(t), TRAFFIC_EGRESS_RESPONSE.getSize());

    final double q = responseTE.getQ();
    final BsStateType state = responseTE.getState();
    final double tTrafficEgress = responseTE.getTTrafficEgress();
    final double tNewState = responseTE.getTNewState();
    final BsStateType nextState = responseTE.getNextState();
    final double w = responseTE.getW();
    final long id = responseTE.getId();
    final double size = responseTE.getSize();

    loggerCustom.logTrafficEgress(t, bs.getId(), id, size, q, w);

    if (state != bs.getState()) loggerCustom.logNewState(t, bs, q, state);

    createEvents(bs, tNewState, tTrafficEgress, nextState);

    bs.addQ(q, t);
    bs.addW(w);
    bs.setState(state);
  }

  private void processNewState(final Event event) {
    final Bs bs = (Bs) event.getEntity();

    final NewStateResponseDto responseNS =
        (NewStateResponseDto)
            bs.communicate(
                new NewStateRequestDto(bs.getNextStateBs()), NEW_STATE_RESPONSE.getSize());

    final double q = responseNS.getQ();
    final BsStateType state = responseNS.getStateReceived();
    final double tTrafficEgress = responseNS.getTTrafficEgress();
    final double tNewState = responseNS.getTNewState();
    final BsStateType nextState = responseNS.getNextState();

    if (state != bs.getState()) {
      loggerCustom.logNewState(t, bs, q, state);
    }

    createEvents(bs, tNewState, tTrafficEgress, nextState);

    bs.setState(state);
  }

  private void createEvents(Bs bs, double tNewState, double tTrafficEgress, BsStateType nextState) {
    if (tNewState >= 0) {
      final Event newState = new Event(t + tNewState, EventType.NEW_STATE, bs);
      eventQueue.add(newState);
      bs.setNextStateBs(nextState);
      bs.setIdEventNextState(newState.getId());
    }

    if (tTrafficEgress > -1) {
      eventQueue.add(new Event(t + tTrafficEgress, EventType.TRAFFIC_EGRESS, bs));
    }
  }
}
