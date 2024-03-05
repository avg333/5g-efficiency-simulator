package basestation;

import static org.assertj.core.api.Assertions.assertThat;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import task.Task;
import types.BsStateType;

class BaseStationStateTest {

  @Test
  void shouldAddAndProcessTasks() {
    final BaseStationState state = new BaseStationState();

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
  void shouldUpdateState(final BsStateType stateType) {
    final BaseStationState state = new BaseStationState();

    assertThat(state.getState()).isEqualTo(BsStateType.OFF);
    assertThat(state.getNextState()).isEqualTo(BsStateType.OFF);

    state.setState(stateType);
    state.setNextState(stateType);
    assertThat(state.getState()).isEqualTo(stateType);
    assertThat(state.getNextState()).isEqualTo(stateType);

    final BsStateType randomState = Instancio.create(BsStateType.class);
    assertThat(state.isCurrentState(randomState)).isEqualTo(stateType == randomState);
  }
}
