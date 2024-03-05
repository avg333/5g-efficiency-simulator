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
import domain.entities.Ue;
import exception.InvalidEventTypeException;
import loggers.BrokerLogger;
import loggers.model.NewStateDtoLog;
import loggers.model.TrafficArrivalDtoLog;
import loggers.model.TrafficEgressDtoLog;
import loggers.model.TrafficIngressDtoLog;
import loggers.model.TrafficRouteDtoLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import routing.BsRouter;
import types.BsStateType;

@Slf4j
@RequiredArgsConstructor
public class Broker implements Runnable {

  private final BrokerConfig config;
  private final RegisterServer server;
  private final BsRouter bsRouter;

  private BrokerState state;
  private BrokerLogger brokerLogger;

  public static void main(final String[] args) {
    new Thread(new BrokerFactory().createBroker(args)).start();
  }

  @Override
  public void run() {
    state = new BrokerState(server.getEntities());
    brokerLogger = new BrokerLogger(config, state);

    while (state.getT() <= config.finalT()) {
      processEvent(state.pollNextElement());
      brokerLogger.upgradeProgress(state.getT());
    }

    server.closeSockets();
    brokerLogger.close();
  }

  private void processEvent(final Event event) {
    switch (event.type()) {
      case TRAFFIC_INGRESS -> processTrafficIngress(event);
      case TRAFFIC_EGRESS -> processTrafficEgress(event);
      case NEW_STATE -> processNewState(event);
      default -> {
        log.error("Type {} not supported. Execution completed", event.type());
        server.closeSockets();
        throw new InvalidEventTypeException(event.type());
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

    final Task task = createNewTask(dto.getSize(), state.getT(), dto.getTUntilNextTask());

    state.addEvent(
        createNewEvent(state.getT() + task.tUntilNextTask(), EventType.TRAFFIC_INGRESS, ue));

    if (task.isEmpty()) {
      return;
    }

    ue.addTask(task, new Position(dto.getX(), dto.getY()));

    brokerLogger.log(new TrafficIngressDtoLog(state.getT(), ue, task));

    final Bs bs = processTrafficRoute(ue, task);

    processTrafficArrive(task, bs);
  }

  /*
   * Choose the best BS to send the task from the UE, returning the chosen BS and logging the event.
   */
  protected Bs processTrafficRoute(final Ue ue, final Task task) {
    final Bs bs = bsRouter.getBs(ue, state.getBsList());
    brokerLogger.log(new TrafficRouteDtoLog(state.getT(), ue, bs, task));
    return bs;
  }

  private void processTrafficArrive(final Task task, final Bs bs) {
    final TrafficArrivalResponseDto responseTA =
        (TrafficArrivalResponseDto)
            bs.communicate(
                new TrafficArrivalRequestDto(task.id(), task.size(), task.tArrivalTime()),
                TRAFFIC_ARRIVAL_RESPONSE.getSize());

    final BsStateType bsState = responseTA.getState();
    final BsStateType bsNextState = responseTA.getNextState();
    final double q = responseTA.getQ();
    final double tTrafficEgress = responseTA.getTTrafficEgress();
    final double tNewState = responseTA.getTNewState();
    final double a = responseTA.getA();

    brokerLogger.log(new TrafficArrivalDtoLog(state.getT(), bs, task, q, a));

    if (bs.getState() == BsStateType.HYSTERESIS) {
      brokerLogger.log(new NewStateDtoLog(state.getT(), bs, q, bsState));
      state.removeEventById(bs.getIdEventNextState()); // How often does this happen?
    } else if (bsState != bs.getState()) {
      brokerLogger.log(new NewStateDtoLog(state.getT(), bs, q, bsState));
    }

    createNewStateEventIfNecessary(bs, tNewState, bsNextState);
    createTrafficEgressEventIfNecessary(bs, tTrafficEgress);

    bs.addQ(q, state.getT());
    bs.setState(bsState);
  }

  private void processTrafficEgress(final Event event) {
    final Bs bs = (Bs) event.entity();

    final TrafficEgressResponseDto responseTE =
        (TrafficEgressResponseDto)
            bs.communicate(
                new TrafficEgressRequestDto(state.getT()), TRAFFIC_EGRESS_RESPONSE.getSize());

    final BsStateType bsState = responseTE.getState();
    final BsStateType bsNextState = responseTE.getNextState();
    final double q = responseTE.getQ();
    final double tTrafficEgress = responseTE.getTTrafficEgress();
    final double tNewState = responseTE.getTNewState();
    final long id = responseTE.getId();
    final double size = responseTE.getSize();
    final double w = responseTE.getW();

    brokerLogger.log(new TrafficEgressDtoLog(state.getT(), bs, id, size, q, w));

    if (bsState != bs.getState()) {
      brokerLogger.log(new NewStateDtoLog(state.getT(), bs, q, bsState));
    }

    createNewStateEventIfNecessary(bs, tNewState, bsNextState);
    createTrafficEgressEventIfNecessary(bs, tTrafficEgress);

    bs.addQ(q, state.getT());
    bs.addW(w);
    bs.setState(bsState);
  }

  private void processNewState(final Event event) {
    final Bs bs = (Bs) event.entity();

    final NewStateResponseDto responseNS =
        (NewStateResponseDto)
            bs.communicate(
                new NewStateRequestDto(bs.getNextStateBs()), NEW_STATE_RESPONSE.getSize());

    final BsStateType bsState = responseNS.getStateReceived();
    final BsStateType bsNextState = responseNS.getNextState();
    final double q = responseNS.getQ();
    final double tTrafficEgress = responseNS.getTTrafficEgress();
    final double tNewState = responseNS.getTNewState();

    if (bsState != bs.getState()) {
      brokerLogger.log(new NewStateDtoLog(state.getT(), bs, q, bsState));
    }

    createNewStateEventIfNecessary(bs, tNewState, bsNextState);
    createTrafficEgressEventIfNecessary(bs, tTrafficEgress);

    bs.setState(bsState);
  }

  private void createNewStateEventIfNecessary(
      final Bs bs, final double tNewState, final BsStateType nextState) {
    if (tNewState != NO_NEXT_STATE.getValue()) {
      final Event newState = createNewEvent(state.getT() + tNewState, NEW_STATE, bs);
      state.addEvent(newState);
      bs.setNextStateBs(nextState, newState.id());
    }
  }

  private void createTrafficEgressEventIfNecessary(final Bs bs, final double tTrafficEgress) {
    if (tTrafficEgress != NO_TASK_TO_PROCESS.getValue()) {
      state.addEvent(createNewEvent(state.getT() + tTrafficEgress, TRAFFIC_EGRESS, bs));
    }
  }
}
