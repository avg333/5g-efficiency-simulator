package basestation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import exception.NoCurrentTaskException;
import exception.NoPendingTasksException;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import task.Task;
import types.BsStateType;

class BaseStationStateTest {

  private BaseStationState state;

  @BeforeEach
  void setUp() {
    state = new BaseStationState();
  }

  @Test
  void shouldAddAndProcessTasks() {
    assertThat(state.isIdle()).isTrue();
    assertThat(state.hasTasksPending()).isFalse();
    assertThat(state.getQ()).isZero();

    final Task taskFirst = Instancio.create(Task.class);
    final double aFirst = state.addTask(taskFirst);
    assertThat(state.isIdle()).isTrue();
    assertThat(state.hasTasksPending()).isTrue();
    assertThat(state.getQ()).isEqualTo(taskFirst.size());
    assertThat(aFirst).isEqualTo(taskFirst.tArrivalTime());

    final Task taskSecond = Instancio.create(Task.class);
    final double aSecond = state.addTask(taskSecond);
    assertThat(state.isIdle()).isTrue();
    assertThat(state.hasTasksPending()).isTrue();
    assertThat(state.getQ()).isEqualTo(taskFirst.size() + taskSecond.size());
    assertThat(aSecond).isEqualTo(taskSecond.tArrivalTime() - taskFirst.tArrivalTime());

    final Task processingTaskFirst = state.processNextTask();
    assertThat(processingTaskFirst).isEqualTo(taskFirst);
    assertThat(state.isIdle()).isFalse();
    assertThat(state.hasTasksPending()).isTrue();
    assertThat(state.getQ()).isEqualTo(taskSecond.size());

    final Task processedTaskFirst = state.processCurrentTask();
    assertThat(processedTaskFirst).isEqualTo(taskFirst);
    assertThat(state.isIdle()).isTrue();
    assertThat(state.hasTasksPending()).isTrue();
    assertThat(state.getQ()).isEqualTo(taskSecond.size());

    final Task processingTaskSecond = state.processNextTask();
    assertThat(processingTaskSecond).isEqualTo(taskSecond);
    assertThat(state.isIdle()).isFalse();
    assertThat(state.hasTasksPending()).isFalse();
    assertThat(state.getQ()).isZero();

    final Task processedTaskSecond = state.processCurrentTask();
    assertThat(processedTaskSecond).isEqualTo(taskSecond);
    assertThat(state.isIdle()).isTrue();
    assertThat(state.hasTasksPending()).isFalse();
    assertThat(state.getQ()).isZero();
  }

  @ParameterizedTest
  @EnumSource(BsStateType.class)
  void shouldUpdateStateSuccessfully(final BsStateType stateType) {
    assertThat(state.getState()).isEqualTo(BsStateType.OFF);
    assertThat(state.getNextState()).isEqualTo(BsStateType.OFF);
    if (stateType != BsStateType.OFF) {
      assertThat(state.isCurrentState(stateType)).isFalse();
    }

    state.setState(stateType);
    state.setNextState(stateType);
    assertThat(state.isCurrentState(stateType)).isTrue();
    assertThat(state.getState()).isEqualTo(stateType);
    assertThat(state.getNextState()).isEqualTo(stateType);
  }

  @ParameterizedTest
  @EnumSource(BsStateType.class)
  void isCurrentStateShouldBeTrueWhenParamStateIsEqualsToBsState(final BsStateType paramState) {
    final BsStateType currentState = Instancio.create(BsStateType.class);

    state.setState(currentState);

    assertThat(state.isCurrentState(paramState)).isEqualTo(paramState == currentState);
  }

  @Test
  void isIdleShouldBeTrueWhenThereIsOneTaskBeingProcessed() {
    assertThat(state.isIdle()).isTrue();

    state.addTask(Instancio.create(Task.class));
    assertThat(state.isIdle()).isTrue();

    state.processNextTask();
    assertThat(state.isIdle()).isFalse();

    state.processCurrentTask();
    assertThat(state.isIdle()).isTrue();
  }

  @Test
  void hasTasksPendingShouldBeTrueWhenTasksQueueIsNotEmpty() {
    assertThat(state.hasTasksPending()).isFalse();

    state.addTask(Instancio.create(Task.class));
    assertThat(state.hasTasksPending()).isTrue();

    state.processNextTask();
    assertThat(state.hasTasksPending()).isFalse();

    state.processCurrentTask();
    assertThat(state.hasTasksPending()).isFalse();
  }

  @Test
  void getQShouldBeEqualToSumOfTasksSizeOfTasksQueue() {
    final List<Task> tasks = Instancio.createList(Task.class);
    final double totalTasksSize = tasks.stream().mapToDouble(Task::size).sum();

    tasks.forEach(task -> state.addTask(task));

    assertThat(state.getQ()).isEqualTo(totalTasksSize);
  }

  @Test
  void shouldThrowNoPendingTasksExceptionWhenTryingToProcessNextTaskWithEmptyTaskQueue() {
    assertThatThrownBy(() -> state.processNextTask()).isInstanceOf(NoPendingTasksException.class);
  }

  @Test
  void shouldThrowNoCurrentTaskExceptionWhenTryingToProcessCurrentTaskWhenThereIsNoCurrentTask() {
    assertThatThrownBy(() -> state.processCurrentTask()).isInstanceOf(NoCurrentTaskException.class);
  }
}
