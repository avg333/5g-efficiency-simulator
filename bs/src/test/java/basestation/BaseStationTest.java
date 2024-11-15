package basestation;

import static communication.model.base.DtoIdentifier.NEW_STATE_RESPONSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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
import communication.ClientCommunicator;
import communication.model.CloseEntityDto;
import communication.model.NewStateRequestDto;
import communication.model.NewStateResponseDto;
import communication.model.RegisterRequestDto;
import communication.model.TrafficArrivalRequestDto;
import communication.model.TrafficArrivalResponseDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficEgressResponseDto;
import communication.model.TrafficIngressRequestDto;
import communication.model.base.Dto;
import domain.Position;
import exception.NotSupportedActionException;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import types.BsStateType;

@ExtendWith(MockitoExtension.class)
class BaseStationTest {

  private static final BaseStationConfig BASE_STATION_CONFIG =
      new BaseStationConfig(AlgorithmMode.NO_COALESCING, 1, 0, 0, 0, 0);

  private static final int MSG_LEN = NEW_STATE_RESPONSE.getSize();
  private static final Position POSITION = Instancio.create(Position.class);
  private static final RegisterRequestDto REGISTER_REQUEST =
      new RegisterRequestDto(BASE_STATION, POSITION.getX(), POSITION.getY());

  @Mock private ClientCommunicator communicator;
  @Captor private ArgumentCaptor<RegisterRequestDto> registerDtoCaptor;

  private BaseStation baseStation;

  @BeforeEach
  void setUp() {
    baseStation = new BaseStation(communicator, POSITION, BASE_STATION_CONFIG);
  }

  @Test
  void shouldReturnMsgLen() {
    assertThat(baseStation.getMsgLen()).isEqualTo(MSG_LEN);
  }

  @Test
  void shouldReturnEntityType() {
    assertThat(baseStation.getEntityType()).isEqualTo(BASE_STATION);
  }

  @Test
  void shouldThrowNotSupportedActionExceptionWhenUserEquipmentProcessReceivesAction() {
    final TrafficIngressRequestDto invalidRequest = new TrafficIngressRequestDto();

    doNothing().when(communicator).register(registerDtoCaptor.capture());
    when(communicator.receiveMessage(MSG_LEN)).thenReturn(invalidRequest);
    doNothing().when(communicator).close();

    assertThatThrownBy(() -> baseStation.run())
        .isInstanceOf(NotSupportedActionException.class)
        .hasMessageContaining(invalidRequest.getIdentifier().name());

    verifyRegister();
    verify(communicator, never()).sendMessage(any());
    verifyClose(0);
  }

  @Test
  void shouldProcessActions() {
    doNothing().when(communicator).register(registerDtoCaptor.capture());
    when(communicator.receiveMessage(MSG_LEN))
        .thenReturn(new TrafficArrivalRequestDto(0, 2, 0)) // t=0
        .thenReturn(new NewStateRequestDto(TO_ON)) // t=0
        .thenReturn(new NewStateRequestDto(ON)) // t=0
        .thenReturn(new TrafficArrivalRequestDto(1, 3, 1)) // t=1
        .thenReturn(new TrafficEgressRequestDto(2.0)) // t=2
        .thenReturn(new TrafficEgressRequestDto(5.0)) // t=5
        .thenReturn(new NewStateRequestDto(HYSTERESIS)) // t=5
        .thenReturn(new NewStateRequestDto(TO_OFF)) // t=5
        .thenReturn(new NewStateRequestDto(OFF)) // t=5
        .thenReturn(new CloseEntityDto());
    final ArgumentCaptor<Dto> responseCaptor = ArgumentCaptor.forClass(Dto.class);
    doNothing().when(communicator).sendMessage(responseCaptor.capture());
    doNothing().when(communicator).close();

    baseStation.run();

    verifyRegister();
    verifyProcess(responseCaptor.getAllValues());
    verifyClose(responseCaptor.getAllValues().size());
  }

  private void verifyProcess(final List<Dto> responses) {
    verifyFirstTrafficArrival((TrafficArrivalResponseDto) responses.get(0));
    verifyFirstNewState((NewStateResponseDto) responses.get(1));
    verifySecondNewState((NewStateResponseDto) responses.get(2));
    verifySecondTrafficArrival((TrafficArrivalResponseDto) responses.get(3));
    verifyFirstTrafficEgress((TrafficEgressResponseDto) responses.get(4));
    verifySecondTrafficEgress((TrafficEgressResponseDto) responses.get(5));
    verifyThirdNewState((NewStateResponseDto) responses.get(6));
    verifyFourthNewState((NewStateResponseDto) responses.get(7));
    verifyFifthNewState((NewStateResponseDto) responses.get(8));
    responses.forEach(response -> verify(communicator).sendMessage(response));
  }

  private void verifyFirstTrafficArrival(final TrafficArrivalResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isEqualTo(2);
    assertThat(response.getState()).isEqualTo(BsStateType.OFF);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isEqualTo(BASE_STATION_CONFIG.tToOn());
    assertThat(response.getNextState()).isEqualTo(TO_ON);
    assertThat(response.getA()).isZero();
  }

  private void verifyFirstNewState(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isEqualTo(2);
    assertThat(response.getStateReceived()).isEqualTo(TO_ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isZero();
    assertThat(response.getNextState()).isEqualTo(BsStateType.ON);
  }

  private void verifySecondNewState(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getStateReceived()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(2);
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(ON);
  }

  private void verifySecondTrafficArrival(final TrafficArrivalResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isEqualTo(3);
    assertThat(response.getState()).isEqualTo(ON);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(ON);
    assertThat(response.getA()).isEqualTo(1);
  }

  private void verifyFirstTrafficEgress(final TrafficEgressResponseDto response) {
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

  private void verifySecondTrafficEgress(final TrafficEgressResponseDto response) {
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

  private void verifyThirdNewState(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getStateReceived()).isEqualTo(HYSTERESIS);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isZero();
    assertThat(response.getNextState()).isEqualTo(TO_OFF);
  }

  private void verifyFourthNewState(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getStateReceived()).isEqualTo(TO_OFF);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isZero();
    assertThat(response.getNextState()).isEqualTo(OFF);
  }

  private void verifyFifthNewState(final NewStateResponseDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getQ()).isZero();
    assertThat(response.getStateReceived()).isEqualTo(OFF);
    assertThat(response.getTTrafficEgress()).isEqualTo(NO_TASK_TO_PROCESS.getValue());
    assertThat(response.getTNewState()).isEqualTo(NO_NEXT_STATE.getValue());
    assertThat(response.getNextState()).isEqualTo(OFF);
  }

  private void verifyRegister() {
    final RegisterRequestDto registerRequestDto = registerDtoCaptor.getValue();
    assertThat(registerRequestDto)
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(REGISTER_REQUEST);
    verify(communicator).register(registerRequestDto);
  }

  private void verifyClose(final int previousCalls) {
    verify(communicator, times(previousCalls + 1)).receiveMessage(MSG_LEN);
    verify(communicator).close();
  }
}
