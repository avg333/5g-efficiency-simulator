package basestation;

import static communication.model.base.DtoIdentifier.TRAFFIC_ARRIVAL_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static types.BsStateType.HYSTERESIS;
import static types.BsStateType.OFF;
import static types.BsStateType.ON;
import static types.BsStateType.TO_OFF;
import static types.BsStateType.TO_ON;
import static types.Constants.NO_NEXT_STATE;
import static types.Constants.NO_TASK_TO_PROCESS;
import static types.EntityType.BASE_STATION;

import algorithm.AlgorithmMode;
import communication.Communicator;
import communication.model.CloseEntityDto;
import communication.model.NewStateRequestDto;
import communication.model.NewStateResponseDto;
import communication.model.TrafficArrivalRequestDto;
import communication.model.TrafficArrivalResponseDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficEgressResponseDto;
import communication.model.TrafficIngressRequestDto;
import communication.model.base.Dto;
import domain.Position;
import domain.Task;
import exception.NotSupportedActionException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import types.BsStateType;

@ExtendWith(MockitoExtension.class)
class BaseStationTest {

  private static final BaseStationConfig BASE_STATION_CONFIG =
      new BaseStationConfig(AlgorithmMode.NO_COALESCING, 1, 0, 0, 0, 0);

  private static final int MSG_LEN = TRAFFIC_ARRIVAL_REQUEST.getSize();

  @Mock private Communicator communicator;

  private BaseStation baseStation;

  private static void verifyFirstTrafficArrivalResponseDto(
      final TrafficArrivalResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isEqualTo(2);
    assertThat(response.getState()).isEqualTo(BsStateType.OFF);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isEqualTo(BASE_STATION_CONFIG.tToOn());
    assertThat(response.getNextState()).isEqualTo(TO_ON);
    assertThat(response.getA()).isZero();
  }

  private static void verifyFirstNewStateResponseDto(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isEqualTo(2);
    assertThat(response.getStateReceived()).isEqualTo(TO_ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isZero();
    assertThat(response.getNextState()).isEqualTo(BsStateType.ON);
  }

  private static void verifySecondNewStateResponseDto(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getStateReceived()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(2);
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(ON);
  }

  private static void verifySecondTrafficArrivalResponseDto(
      final TrafficArrivalResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isEqualTo(3);
    assertThat(response.getState()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(ON);
    assertThat(response.getA()).isEqualTo(1);
  }

  private static void verifyFirstTrafficEgressResponseDto(final TrafficEgressResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getState()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(3);
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(ON);
    assertThat(response.getW()).isZero();
    assertThat(response.getId()).isZero();
    assertThat(response.getSize()).isEqualTo(2);
  }

  private static void verifySecondTrafficEgressResponseDto(
      final TrafficEgressResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getState()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isZero();
    assertThat(response.getNextState()).isEqualTo(HYSTERESIS);
    assertThat(response.getW()).isEqualTo(1);
    assertThat(response.getId()).isEqualTo(1);
    assertThat(response.getSize()).isEqualTo(3);
  }

  private static void verifyThirdNewStateResponseDto(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getStateReceived()).isEqualTo(HYSTERESIS);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isZero();
    assertThat(response.getNextState()).isEqualTo(TO_OFF);
  }

  private static void verifyFourthNewStateResponseDto(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getStateReceived()).isEqualTo(TO_OFF);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isZero();
    assertThat(response.getNextState()).isEqualTo(OFF);
  }

  private static void verifyFifthNewStateResponseDto(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getStateReceived()).isEqualTo(OFF);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(OFF);
  }

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
  void shouldProcessActions() {
    this.baseStation =
        new BaseStation(Instancio.create(Position.class), communicator, BASE_STATION_CONFIG);

    when(communicator.receiveMessage(MSG_LEN))
        .thenReturn(new TrafficArrivalRequestDto(new Task(0, 2, 0, 0))) // 0
        .thenReturn(new NewStateRequestDto(TO_ON)) // 0
        .thenReturn(new NewStateRequestDto(ON)) // 0
        .thenReturn(new TrafficArrivalRequestDto(new Task(1, 3, 1, 0))) // 1
        .thenReturn(new TrafficEgressRequestDto(2.0)) // 2
        .thenReturn(new TrafficEgressRequestDto(5.0)) // 5
        .thenReturn(new NewStateRequestDto(HYSTERESIS)) // 5
        .thenReturn(new NewStateRequestDto(TO_OFF)) // 5
        .thenReturn(new NewStateRequestDto(OFF)) // 5
        .thenReturn(new CloseEntityDto());
    doNothing().when(communicator).close();

    final ArgumentCaptor<Dto> responseCaptor = ArgumentCaptor.forClass(Dto.class);
    doNothing().when(communicator).sendMessage(responseCaptor.capture());

    baseStation.run();

    verifyFirstTrafficArrivalResponseDto(
        (TrafficArrivalResponseDto) responseCaptor.getAllValues().get(0));
    verifyFirstNewStateResponseDto((NewStateResponseDto) responseCaptor.getAllValues().get(1));
    verifySecondNewStateResponseDto((NewStateResponseDto) responseCaptor.getAllValues().get(2));
    verifySecondTrafficArrivalResponseDto(
        (TrafficArrivalResponseDto) responseCaptor.getAllValues().get(3));
    verifyFirstTrafficEgressResponseDto(
        (TrafficEgressResponseDto) responseCaptor.getAllValues().get(4));
    verifySecondTrafficEgressResponseDto(
        (TrafficEgressResponseDto) responseCaptor.getAllValues().get(5));
    verifyThirdNewStateResponseDto((NewStateResponseDto) responseCaptor.getAllValues().get(6));
    verifyFourthNewStateResponseDto((NewStateResponseDto) responseCaptor.getAllValues().get(7));
    verifyFifthNewStateResponseDto((NewStateResponseDto) responseCaptor.getAllValues().get(8));

    for (int i = 0; i < responseCaptor.getAllValues().size(); i++) {
      verify(communicator).sendMessage(responseCaptor.getAllValues().get(i));
    }

    verify(communicator, times(responseCaptor.getAllValues().size() + 1)).receiveMessage(MSG_LEN);
    verify(communicator).close();
  }
}
