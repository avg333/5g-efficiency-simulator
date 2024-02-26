package basestation;

import static communication.model.base.DtoIdentifier.NEW_STATE_RESPONSE;
import static communication.model.base.DtoIdentifier.TRAFFIC_ARRIVAL_REQUEST;
import static communication.model.base.DtoIdentifier.TRAFFIC_EGRESS_REQUEST;
import static java.util.Objects.isNull;
import static types.Constants.NO_NEXT_STATE;
import static types.Constants.NO_TASK_TO_PROCESS;
import static types.EntityType.BASE_STATION;

import communication.Communicator;
import communication.model.NewStateRequestDto;
import communication.model.NewStateResponseDto;
import communication.model.TrafficArrivalRequestDto;
import communication.model.TrafficArrivalResponseDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficEgressResponseDto;
import communication.model.base.Dto;
import domain.Position;
import entity.BaseEntity;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import task.Task;
import types.BsStateType;
import types.EntityType;

@Slf4j
public class BaseStation extends BaseEntity {

  private static final int TIME_TO_ENTER_HYSTERESIS = 0;
  private static final int TIME_TO_EXIT_HYSTERESIS = 0;
  private static final int TIME_TO_SUSPEND = 0;
  private static final BsStateType DEFAULT_NEXT_STATE = BsStateType.OFF;

  private static final int MSG_LEN =
      getMaxMsgLen(TRAFFIC_ARRIVAL_REQUEST, TRAFFIC_EGRESS_REQUEST, NEW_STATE_RESPONSE);

  private final BaseStationConfig baseStationConfig;

  private final Deque<Task> tasksPending = new ArrayDeque<>();
  private BsStateType state = DEFAULT_NEXT_STATE;
  private BsStateType nextState = DEFAULT_NEXT_STATE;
  private Task currentTask = null;
  private double lastTaskArrivalTime = 0.0;

  public BaseStation(
      Position position, Communicator communicator, BaseStationConfig baseStationConfig) {
    super(position, communicator);
    this.baseStationConfig = baseStationConfig;

    log.info("Registered in {}", communicator);
  }

  public static void main(String[] args) {
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
  protected final void processAction(final Dto dto) {
    switch (dto.getIdentifier()) {
      case TRAFFIC_ARRIVAL_REQUEST -> processTrafficArrival((TrafficArrivalRequestDto) dto);
      case TRAFFIC_EGRESS_REQUEST -> processTrafficEgress((TrafficEgressRequestDto) dto);
      case NEW_STATE_REQUEST -> processNewState((NewStateRequestDto) dto);
      default -> processNotSupportedAction(dto);
    }
  }

  /*
   * This method receives a task from the broker and adds it to the list of tasks pending.
   * After that it communicates the following to the broker one of the following:
   * - The next state the base station will transition to and when will take to change to that state
   * - When the next task will be processed.
   * - Nothing if the base station has no tasks to process.
   */
  protected void processTrafficArrival(final TrafficArrivalRequestDto request) {
    final Task task =
        new Task(request.getTaskId(), request.getTaskSize(), request.getTaskTArrivalTime());
    log.debug("Received task {}", task);

    tasksPending.addLast(task);

    final double tNewState = decideActivationAndScheduleIfPossible();

    final double tTrafficEgress = startProcessingTaskIfPossible();

    final double q = getQ();
    final double a = getA(task.tArrivalTime());

    lastTaskArrivalTime = task.tArrivalTime();

    sendMessage(new TrafficArrivalResponseDto(q, state, tTrafficEgress, tNewState, nextState, a));
  }

  /*
   * This method finish processing the task that is currently being processed.
   * After that it communicates the following to the broker one of the following:
   * - The next state the base station will transition to and when will take to change to that state
   * This only happens if the base station is not processing a task and there are no tasks pending.
   * - When the next task will be processed if there are tasks pending.
   */
  protected void processTrafficEgress(final TrafficEgressRequestDto request) {
    final double currentT = request.getT();
    log.debug("Processed task {} at {}", currentTask, currentT);

    final long id = currentTask.id();
    final double size = currentTask.size();
    final double w = getW(currentT);

    currentTask = null;

    final double tNewState = decideSuspensionAndScheduleIfPossible();

    final double tTrafficEgress = startProcessingTaskIfPossible();

    final double q = getQ();

    sendMessage(
        new TrafficEgressResponseDto(q, state, tTrafficEgress, tNewState, nextState, w, id, size));
  }

  /*
   * This method changes the current state to the indicated state.
   * After that it communicates the following to the broker one of the following:
   * - The next state the base station will transition to and when will take to change to that state
   * - When the next task will be processed.
   * - Nothing if the base station has no tasks to process.
   */
  protected void processNewState(final NewStateRequestDto request) {
    final BsStateType stateReceived = request.getState();
    log.debug("Updated to STATE={}", stateReceived);

    final double tNewState = setNewStateAndScheduleNextStateChange(stateReceived);

    final double tTrafficEgress = startProcessingTaskIfPossible();

    final double q = getQ();

    sendMessage(new NewStateResponseDto(q, stateReceived, tTrafficEgress, tNewState, nextState));
  }

  private double startProcessingTaskIfPossible() {
    final boolean isNotProcessingTask = isNull(currentTask);
    final boolean existsTaskToProcess = !tasksPending.isEmpty();
    final boolean bsIsActive = state == BsStateType.ON;

    if (isNotProcessingTask && existsTaskToProcess && bsIsActive) {
      currentTask = tasksPending.removeFirst();
      return currentTask.size() / baseStationConfig.c();
    }

    return NO_TASK_TO_PROCESS.getValue();
  }

  private double decideActivationAndScheduleIfPossible() {
    final boolean existsTaskToProcess = !tasksPending.isEmpty();
    final boolean bsIsOnHysteresis = state == BsStateType.HYSTERESIS;
    final boolean bsIsNotOff = state != BsStateType.OFF;

    if (existsTaskToProcess && bsIsOnHysteresis) {
      nextState = BsStateType.ON;
      return TIME_TO_EXIT_HYSTERESIS;
    } else if (bsIsNotOff) {
      return NO_NEXT_STATE.getValue();
    }

    final Optional<BsStateType> candidateNextState = getNextActiveState(existsTaskToProcess);
    if (candidateNextState.isEmpty()) {
      return NO_NEXT_STATE.getValue();
    }

    nextState = candidateNextState.get();
    return TIME_TO_SUSPEND;
  }

  private Optional<BsStateType> getNextActiveState(final boolean existsTaskToProcess) {
    return switch (baseStationConfig.mode()) {
      case NO_COALESCING -> existsTaskToProcess ? Optional.of(BsStateType.TO_ON) : Optional.empty();
      case SIZE_BASED_COALESCING -> activationSizeBasedCoalescing();
      case TIME_BASED_COALESCING ->
          existsTaskToProcess ? Optional.of(BsStateType.WAITING_TO_ON) : Optional.empty();
      case FIXED_COALESCING -> activationFixedCoalescing(existsTaskToProcess);
    };
  }

  private Optional<BsStateType> activationSizeBasedCoalescing() {
    final boolean taskSizeIsGreaterThanAlgorithmParam = getQ() > baseStationConfig.algorithmParam();
    return taskSizeIsGreaterThanAlgorithmParam ? Optional.of(BsStateType.TO_ON) : Optional.empty();
  }

  private Optional<BsStateType> activationFixedCoalescing(final boolean existsTaskToProcess) {
    return existsTaskToProcess
        ? Optional.of(BsStateType.WAITING_TO_ON)
        : Optional.of(BsStateType.OFF);
  }

  private double decideSuspensionAndScheduleIfPossible() {
    final boolean isNotProcessingTask = isNull(currentTask);
    final boolean notExistsTaskToProcess = tasksPending.isEmpty();
    final boolean bsIsActive = state == BsStateType.ON;

    if (isNotProcessingTask && notExistsTaskToProcess && bsIsActive) {
      nextState = BsStateType.HYSTERESIS;
      return TIME_TO_ENTER_HYSTERESIS;
    }

    return NO_NEXT_STATE.getValue();
  }

  private double setNewStateAndScheduleNextStateChange(final BsStateType stateReceived) {
    double tNewState = NO_NEXT_STATE.getValue();

    switch (stateReceived) {
      case ON, OFF -> state = stateReceived;
      case TO_ON -> {
        state = stateReceived;
        nextState = BsStateType.ON;
        tNewState = baseStationConfig.tToOn();
      }
      case TO_OFF -> {
        if (state == BsStateType.HYSTERESIS) {
          state = stateReceived;
          nextState = BsStateType.OFF;
          tNewState = baseStationConfig.tToOff();
        }
      }
      case WAITING_TO_ON -> {
        state = stateReceived;
        nextState = BsStateType.TO_ON;
        tNewState = baseStationConfig.algorithmParam();
      }
      case HYSTERESIS -> {
        state = stateReceived;
        nextState = BsStateType.TO_OFF;
        tNewState = baseStationConfig.tHysteresis();
      }
    }
    return tNewState;
  }

  // Sum of the size of the tasks pending
  private double getQ() {
    // TODO Add q sum to avoid recalculation
    return tasksPending.stream().mapToDouble(Task::size).sum();
  }

  // Time since the last task arrived
  private double getA(final double currentT) {
    return currentT - lastTaskArrivalTime;
  }

  // TODO Explain this function
  private double getW(final double currentT) {
    return currentT - currentTask.tArrivalTime() - currentTask.size() / baseStationConfig.c();
  }
}
