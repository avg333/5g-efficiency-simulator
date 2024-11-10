package userequipment;

import static communication.model.base.DtoIdentifier.TRAFFIC_INGRESS_REQUEST;
import static distribution.DistributionMode.DETERMINISTIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static types.EntityType.USER_EQUIPMENT;

import communication.ClientCommunicator;
import communication.model.CloseEntityDto;
import communication.model.RegisterRequestDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficIngressRequestDto;
import communication.model.TrafficIngressResponseDto;
import communication.model.base.Dto;
import distribution.Distribution;
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
import task.TaskGenerator;

@ExtendWith(MockitoExtension.class)
class UserEquipmentTest {

  private static final int MSG_LEN = TRAFFIC_INGRESS_REQUEST.getSize();
  private static final double X_START = Instancio.create(Integer.class);
  private static final double Y_START = Instancio.create(Integer.class);
  private static final int MOVE_INCREMENT = Instancio.create(Integer.class);
  private static final int TASK_SIZE = Instancio.create(Integer.class);
  private static final int TASK_DELAY = Instancio.create(Integer.class);
  private static final RegisterRequestDto REGISTER_REQUEST =
      new RegisterRequestDto(USER_EQUIPMENT, X_START, Y_START);

  private UserEquipment userEquipment;

  @Mock private ClientCommunicator communicator;
  @Captor private ArgumentCaptor<RegisterRequestDto> registerDtoCaptor;

  @BeforeEach
  void setUp() {
    this.userEquipment =
        new UserEquipment(
            communicator,
            new Position(X_START, Y_START),
            new Distribution(DETERMINISTIC, MOVE_INCREMENT, 0),
            new TaskGenerator(
                new Distribution(DETERMINISTIC, TASK_SIZE, 0),
                new Distribution(DETERMINISTIC, TASK_DELAY, 0)));
  }

  @Test
  void shouldReturnMsgLen() {
    assertThat(userEquipment.getMsgLen()).isEqualTo(MSG_LEN);
  }

  @Test
  void shouldReturnEntityType() {
    assertThat(userEquipment.getEntityType()).isEqualTo(USER_EQUIPMENT);
  }

  @Test
  void shouldThrowNotSupportedActionExceptionWhenReceivesUnsupportedAction() {
    final TrafficEgressRequestDto invalidRequest =
        new TrafficEgressRequestDto(Instancio.create(Double.class));

    doNothing().when(communicator).register(registerDtoCaptor.capture());
    when(communicator.receiveMessage(MSG_LEN)).thenReturn(invalidRequest);
    doNothing().when(communicator).close();

    assertThatThrownBy(() -> userEquipment.run())
        .isInstanceOf(NotSupportedActionException.class)
        .hasMessageContaining(invalidRequest.getIdentifier().name());

    verifyRegister();
    verify(communicator, never()).sendMessage(any());
    verifyClose(0);
  }

  @Test
  void shouldProcessTrafficIngressSuccessfully() {
    doNothing().when(communicator).register(registerDtoCaptor.capture());
    when(communicator.receiveMessage(MSG_LEN))
        .thenReturn(new TrafficIngressRequestDto())
        .thenReturn(new TrafficIngressRequestDto())
        .thenReturn(new CloseEntityDto());
    final ArgumentCaptor<Dto> responseCaptor = ArgumentCaptor.forClass(Dto.class);
    doNothing().when(communicator).sendMessage(responseCaptor.capture());
    doNothing().when(communicator).close();

    userEquipment.run();

    verifyRegister();
    verifyTrafficIngresses(responseCaptor.getAllValues());
    verifyClose(responseCaptor.getAllValues().size());
  }

  private void verifyTrafficIngresses(final List<Dto> trafficIngresses) {
    for (int i = 0; i < trafficIngresses.size(); i++) {
      verifyTrafficIngress((TrafficIngressResponseDto) trafficIngresses.get(i), i + 1);
    }
  }

  private void verifyTrafficIngress(final TrafficIngressResponseDto response, final int time) {
    assertThat(response).isNotNull();
    assertThat(response.getX()).isEqualTo(X_START + time * MOVE_INCREMENT);
    assertThat(response.getY()).isEqualTo(Y_START + time * MOVE_INCREMENT);
    assertThat(response.getSize()).isEqualTo(TASK_SIZE);
    assertThat(response.getTUntilNextTask()).isEqualTo(TASK_DELAY);
    verify(communicator).sendMessage(response);
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
