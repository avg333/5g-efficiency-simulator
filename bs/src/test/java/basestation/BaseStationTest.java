package basestation;

import static communication.model.base.DtoIdentifier.TRAFFIC_ARRIVAL_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static types.BsStateType.HYSTERESIS;
import static types.BsStateType.ON;
import static types.BsStateType.TO_ON;
import static types.Constants.NO_NEXT_STATE;
import static types.Constants.NO_TASK_TO_PROCESS;
import static types.EntityType.BASE_STATION;

import algorithm.AlgorithmMode;
import communication.Communicator;
import communication.model.CloseEntityDto;
import communication.model.TrafficArrivalRequestDto;
import communication.model.TrafficArrivalResponseDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficEgressResponseDto;
import communication.model.TrafficIngressRequestDto;
import domain.Position;
import domain.Task;
import exception.NotSupportedActionException;
import java.lang.reflect.Field;
import java.util.Deque;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import types.BsStateType;

@ExtendWith(MockitoExtension.class)
class BaseStationTest {

  private static final int T_TO_ON = 0;

  private static final int C = 1;

  private static final int MSG_LEN = TRAFFIC_ARRIVAL_REQUEST.getSize();

  @Mock private Communicator communicator;

  private BaseStation baseStation;

  @Test
  void shouldReturnMsgLen() {
    this.baseStation =
        new BaseStation(
            Instancio.create(Position.class),
            communicator,
            Instancio.create(BaseStationConfig.class));
    assertThat(baseStation.getMsgLen()).isEqualTo(MSG_LEN);
  }

  @Test
  void shouldReturnEntityType() {
    this.baseStation =
        new BaseStation(
            Instancio.create(Position.class),
            communicator,
            Instancio.create(BaseStationConfig.class));
    assertThat(baseStation.getEntityType()).isEqualTo(BASE_STATION);
  }

  @Test
  void shouldThrowNotSupportedActionExceptionWhenUserEquipmentProcessReceivesAction() {
    this.baseStation =
        new BaseStation(
            Instancio.create(Position.class),
            communicator,
            Instancio.create(BaseStationConfig.class));
    final TrafficIngressRequestDto invalidRequest = new TrafficIngressRequestDto();
    when(communicator.receiveMessage(MSG_LEN)).thenReturn(invalidRequest);
    doNothing().when(communicator).close();

    assertThatThrownBy(() -> baseStation.run())
        .isInstanceOf(NotSupportedActionException.class)
        .hasMessageContaining("Action not supported: " + invalidRequest.getIdentifier());

    verify(communicator).receiveMessage(MSG_LEN);
    verify(communicator).close();
  }

  @Test
  void shouldProcessTrafficIngress() {
    this.baseStation =
        new BaseStation(
            Instancio.create(Position.class),
            communicator,
            new BaseStationConfig(AlgorithmMode.NO_COALESCING, 1, 0, 0, 0, 0));

    when(communicator.receiveMessage(MSG_LEN))
        .thenReturn(new TrafficArrivalRequestDto(new Task(0, 1, 0, 0)))
        .thenReturn(new TrafficArrivalRequestDto(new Task(0, 1, 1, 0)))
        .thenReturn(new CloseEntityDto());
    doNothing().when(communicator).close();

    final ArgumentCaptor<TrafficArrivalResponseDto> responseCaptor =
        ArgumentCaptor.forClass(TrafficArrivalResponseDto.class);
    doNothing().when(communicator).sendMessage(responseCaptor.capture());

    baseStation.run();

    for (int i = 0; i < responseCaptor.getAllValues().size(); i++) {
      verifyTrafficArrivalResponseDto(responseCaptor.getAllValues().get(i), i + 1);
      verify(communicator).sendMessage(responseCaptor.getAllValues().get(i));
    }

    verify(communicator, times(responseCaptor.getAllValues().size() + 1)).receiveMessage(MSG_LEN);
    verify(communicator).close();
  }

  private static void verifyTrafficArrivalResponseDto(
      final TrafficArrivalResponseDto response, final int time) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isEqualTo(time * 1.0);
    assertThat(response.getState()).isEqualTo(BsStateType.OFF);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isEqualTo(T_TO_ON);
    assertThat(response.getNextState()).isEqualTo(TO_ON);
    assertThat(response.getA()).isEqualTo(time - 1);
  }

  @Test
  void shouldProcessTrafficEgress() throws NoSuchFieldException, IllegalAccessException {
    this.baseStation =
        new BaseStation(
            Instancio.create(Position.class),
            communicator,
            new BaseStationConfig(AlgorithmMode.NO_COALESCING, C, 0, 0, 0, 0));

    preSetTrafficEgressBsState();

    when(communicator.receiveMessage(MSG_LEN))
        .thenReturn(new TrafficEgressRequestDto(1.0))
        .thenReturn(new TrafficEgressRequestDto(3.0))
        .thenReturn(new TrafficEgressRequestDto(6.0))
        .thenReturn(new CloseEntityDto());
    doNothing().when(communicator).close();

    final ArgumentCaptor<TrafficEgressResponseDto> responseCaptor =
        ArgumentCaptor.forClass(TrafficEgressResponseDto.class);
    doNothing().when(communicator).sendMessage(responseCaptor.capture());

    baseStation.run();

    verifyFirstTrafficEgressResponseDto(responseCaptor.getAllValues().get(0));
    verifySecondTrafficEgressResponseDto(responseCaptor.getAllValues().get(1));
    verifyThirdTrafficEgressResponseDto(responseCaptor.getAllValues().get(2));
    for (int i = 0; i < responseCaptor.getAllValues().size(); i++) {
      verify(communicator).sendMessage(responseCaptor.getAllValues().get(i));
    }

    verify(communicator, times(responseCaptor.getAllValues().size() + 1)).receiveMessage(MSG_LEN);
    verify(communicator).close();
  }

  private void preSetTrafficEgressBsState() throws NoSuchFieldException, IllegalAccessException {
    Field stateField = BaseStation.class.getDeclaredField("state");
    stateField.setAccessible(true);
    stateField.set(baseStation, ON);
    Field nextStateField = BaseStation.class.getDeclaredField("nextState");
    nextStateField.setAccessible(true);
    nextStateField.set(baseStation, ON);
    Field currentTaskField = BaseStation.class.getDeclaredField("currentTask");
    currentTaskField.setAccessible(true);
    currentTaskField.set(baseStation, new task.Task(1, 1, 0));
    Field tasksPendingField = BaseStation.class.getDeclaredField("tasksPending");
    tasksPendingField.setAccessible(true);
    Deque<task.Task> tasks = (Deque<task.Task>) tasksPendingField.get(baseStation);
    tasks.add(new task.Task(2, 2, 0));
    tasks.add(new task.Task(3, 3, 0));
  }

  private static void verifyFirstTrafficEgressResponseDto(final TrafficEgressResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isEqualTo(3.0);
    assertThat(response.getState()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(2);
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(ON);
    assertThat(response.getW()).isZero();
    assertThat(response.getId()).isEqualTo(1);
    assertThat(response.getSize()).isEqualTo(1);
  }

  private static void verifySecondTrafficEgressResponseDto(
      final TrafficEgressResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getState()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(3);
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(ON);
    assertThat(response.getW()).isEqualTo(1);
    assertThat(response.getId()).isEqualTo(2);
    assertThat(response.getSize()).isEqualTo(2);
  }

  private static void verifyThirdTrafficEgressResponseDto(final TrafficEgressResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getState()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isZero();
    assertThat(response.getNextState()).isEqualTo(HYSTERESIS);
    assertThat(response.getW()).isEqualTo(3);
    assertThat(response.getId()).isEqualTo(3);
    assertThat(response.getSize()).isEqualTo(3);
  }

  @Test
  void shouldProcessNewState() {
    // TODO
  }
}
