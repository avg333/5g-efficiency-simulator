package basestation;

import static communication.model.base.DtoIdentifier.NEW_STATE_RESPONSE;
import static communication.model.base.DtoIdentifier.TRAFFIC_ARRIVAL_REQUEST;
import static communication.model.base.DtoIdentifier.TRAFFIC_EGRESS_REQUEST;
import static types.BsStateType.HYSTERESIS;
import static types.BsStateType.OFF;
import static types.BsStateType.ON;
import static types.BsStateType.TO_OFF;
import static types.BsStateType.TO_ON;
import static types.Constants.NO_NEXT_STATE;
import static types.Constants.NO_TASK_TO_PROCESS;
import static types.EntityType.BASE_STATION;

import communication.ClientCommunicator;
import communication.model.NewStateRequestDto;
import communication.model.NewStateResponseDto;
import communication.model.TrafficArrivalRequestDto;
import communication.model.TrafficArrivalResponseDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficEgressResponseDto;
import communication.model.base.Dto;
import domain.Position;
import entity.BaseEntity;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import task.Task;
import types.BsStateType;
import types.EntityType;
import utils.BaseStationUtils;

@Slf4j
public class BaseStation extends BaseEntity {

  private static final EntityType TYPE = BASE_STATION;

  private static final int TIME_TO_ENTER_HYSTERESIS = 0;
  private static final int TIME_TO_EXIT_HYSTERESIS = 0;
  private static final int TIME_TO_SUSPEND = 0;

  private static final int MSG_LEN =
      getMaxMsgLen(TRAFFIC_ARRIVAL_REQUEST, TRAFFIC_EGRESS_REQUEST, NEW_STATE_RESPONSE);

  private final BaseStationConfig baseStationConfig;

  private final BaseStationState state = new BaseStationState();

  public BaseStation(
      final ClientCommunicator communicator,
      final Position position,
      final BaseStationConfig baseStationConfig) {
    super(TYPE, communicator, position);
    this.baseStationConfig = baseStationConfig;
    log.info(
        "BaseStation created with communicator: {}, position: {}, baseStationConfig: {}",
        communicator,
        position,
        baseStationConfig);
  }

  public static void main(final String[] args) {
    new Thread(new BaseStationFactory().createBaseStation()).start();
  }

  @Override
  protected final int getMsgLen() {
    return MSG_LEN;
  }

  @Override
  protected final EntityType getEntityType() {
    return BASE_STATION;
  }

  @Override
  protected final Dto processAction(final Dto dto) {
    return switch (dto.getIdentifier()) {
      case TRAFFIC_ARRIVAL_REQUEST -> processTrafficArrival((TrafficArrivalRequestDto) dto);
      case TRAFFIC_EGRESS_REQUEST -> processTrafficEgress((TrafficEgressRequestDto) dto);
      case NEW_STATE_REQUEST -> processNewState((NewStateRequestDto) dto);
      default -> processNotSupportedAction(dto);
    };
  }

  /*
   * This method receives a task from the broker and adds it to the list of tasks pending.
   * After that it communicates the following to the broker one of the following:
   * - The next state the base station will transition to and when will take to change to that state
   * - When the next task will be processed.
   * - Nothing if the base station has no tasks to process.
   */
  protected Dto processTrafficArrival(final TrafficArrivalRequestDto request) {
    final Task task =
        new Task(request.getTaskId(), request.getTaskSize(), request.getTaskTArrivalTime());
    log.debug("Received task {}", task);

    final double a = state.addTask(task);

    final double tNewState = decideActivationAndScheduleIfPossible();

    final double tTrafficEgress = startProcessingTaskIfPossible();

    return new TrafficArrivalResponseDto(
        state.getState(), state.getNextState(), state.getQ(), tTrafficEgress, tNewState, a);
  }

  /*
   * This method finish processing the task that is currently being processed.
   * After that it communicates the following to the broker one of the following:
   * - The next state the base station will transition to and when will take to change to that state
   * This only happens if the base station is not processing a task and there are no tasks pending.
   * - When the next task will be processed if there are tasks pending.
   */
  protected Dto processTrafficEgress(final TrafficEgressRequestDto request) {
    final double currentT = request.getT();
    final Task processedTask = state.processCurrentTask();

    log.debug("Processed task {} at {}", processedTask, currentT);

    final double tNewState = decideSuspensionAndScheduleIfPossible();

    final double tTrafficEgress = startProcessingTaskIfPossible();

    return new TrafficEgressResponseDto(
        state.getState(),
        state.getNextState(),
        state.getQ(),
        tTrafficEgress,
        tNewState,
        processedTask.id(),
        processedTask.size(),
        BaseStationUtils.calculateW(currentT, processedTask, baseStationConfig.c()));
  }

  /*
   * This method changes the current state to the indicated state.
   * After that it communicates the following to the broker one of the following:
   * - The next state the base station will transition to and when will take to change to that state
   * - When the next task will be processed.
   * - Nothing if the base station has no tasks to process.
   */
  protected Dto processNewState(final NewStateRequestDto request) {
    final BsStateType stateReceived = request.getState();
    log.debug("Updated to STATE={}", stateReceived);

    final double tNewState = setNewStateAndScheduleNextStateChange(stateReceived);

    final double tTrafficEgress = startProcessingTaskIfPossible();

    return new NewStateResponseDto(
        stateReceived, state.getNextState(), state.getQ(), tTrafficEgress, tNewState);
  }

  private double startProcessingTaskIfPossible() {
    if (state.isIdle() && state.hasTasksPending() && state.isCurrentState(ON)) {
      return state.processNextTask().size() / baseStationConfig.c();
    }

    return NO_TASK_TO_PROCESS.getValue();
  }

  private double decideActivationAndScheduleIfPossible() {
    final boolean existsTaskToProcess = state.hasTasksPending();

    if (existsTaskToProcess && state.isCurrentState(HYSTERESIS)) {
      state.setNextState(ON);
      return TIME_TO_EXIT_HYSTERESIS;
    } else if (!state.isCurrentState(OFF)) {
      return NO_NEXT_STATE.getValue();
    }

    final Optional<BsStateType> candidateNextState = getNextActiveState(existsTaskToProcess);
    if (candidateNextState.isEmpty()) {
      return NO_NEXT_STATE.getValue();
    }

    state.setNextState(candidateNextState.get());
    return TIME_TO_SUSPEND;
  }

  private Optional<BsStateType> getNextActiveState(final boolean existsTaskToProcess) {
    return switch (baseStationConfig.mode()) {
      case NO_COALESCING -> existsTaskToProcess ? Optional.of(TO_ON) : Optional.empty();
      case SIZE_BASED_COALESCING -> activationSizeBasedCoalescing();
      case TIME_BASED_COALESCING ->
          existsTaskToProcess ? Optional.of(BsStateType.WAITING_TO_ON) : Optional.empty();
      case FIXED_COALESCING -> activationFixedCoalescing(existsTaskToProcess);
    };
  }

  private Optional<BsStateType> activationSizeBasedCoalescing() {
    return state.getQ() > baseStationConfig.algorithmParam()
        ? Optional.of(TO_ON)
        : Optional.empty();
  }

  private Optional<BsStateType> activationFixedCoalescing(final boolean existsTaskToProcess) {
    return existsTaskToProcess
        ? Optional.of(BsStateType.WAITING_TO_ON)
        : Optional.of(BsStateType.OFF);
  }

  private double decideSuspensionAndScheduleIfPossible() {
    if (state.isIdle() && !state.hasTasksPending() && state.isCurrentState(ON)) {
      state.setNextState(BsStateType.HYSTERESIS);
      return TIME_TO_ENTER_HYSTERESIS;
    }

    return NO_NEXT_STATE.getValue();
  }

  private double setNewStateAndScheduleNextStateChange(final BsStateType stateReceived) {
    double tNewState = NO_NEXT_STATE.getValue();

    switch (stateReceived) {
      case ON, OFF -> state.setState(stateReceived);
      case TO_ON -> {
        state.setState(stateReceived);
        state.setNextState(ON);
        tNewState = baseStationConfig.tToOn();
      }
      case TO_OFF -> {
        if (state.isCurrentState(HYSTERESIS)) {
          state.setState(stateReceived);
          state.setNextState(OFF);
          tNewState = baseStationConfig.tToOff();
        }
      }
      case WAITING_TO_ON -> {
        state.setState(stateReceived);
        state.setNextState(TO_ON);
        tNewState = baseStationConfig.algorithmParam();
      }
      case HYSTERESIS -> {
        state.setState(stateReceived);
        state.setNextState(TO_OFF);
        tNewState = baseStationConfig.tHysteresis();
      }
    }
    return tNewState;
  }
}
