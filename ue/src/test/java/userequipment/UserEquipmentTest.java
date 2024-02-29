package userequipment;

import static communication.model.base.DtoIdentifier.TRAFFIC_INGRESS_REQUEST;
import static distribution.DistributionMode.DETERMINISTIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static types.EntityType.USER_EQUIPMENT;

import communication.ClientCommunicator;
import communication.model.CloseEntityDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficIngressRequestDto;
import communication.model.TrafficIngressResponseDto;
import communication.model.base.Dto;
import distribution.Distribution;
import domain.Position;
import exception.NotSupportedActionException;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

  @Mock private ClientCommunicator communicator;
  private UserEquipment userEquipment;

  private static void verifyTrafficIngress(
      final TrafficIngressResponseDto response, final int time) {
    assertThat(response).isNotNull();
    assertThat(response.getX()).isEqualTo(X_START + time * MOVE_INCREMENT);
    assertThat(response.getY()).isEqualTo(Y_START + time * MOVE_INCREMENT);
    assertThat(response.getSize()).isEqualTo(TASK_SIZE);
    assertThat(response.getTUntilNextTask()).isEqualTo(TASK_DELAY);
  }

  @BeforeEach
  void setUp() {
    final Distribution mobilityDist = new Distribution(DETERMINISTIC, MOVE_INCREMENT, 0);
    final Distribution sizeDist = new Distribution(DETERMINISTIC, TASK_SIZE, 0);
    final Distribution delayDist = new Distribution(DETERMINISTIC, TASK_DELAY, 0);
    final TaskGenerator taskGenerator = new TaskGenerator(sizeDist, delayDist);
    this.userEquipment =
        new UserEquipment(
            communicator, new Position(X_START, Y_START), mobilityDist, taskGenerator);
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
  void shouldThrowNotSupportedActionExceptionWhenUserEquipmentProcessReceivesAction() {
    final TrafficEgressRequestDto invalidRequest =
        new TrafficEgressRequestDto(Instancio.create(Double.class));
    when(communicator.receiveMessage(MSG_LEN)).thenReturn(invalidRequest);
    doNothing().when(communicator).close();

    assertThatThrownBy(() -> userEquipment.run())
        .isInstanceOf(NotSupportedActionException.class)
        .hasMessageContaining("Action not supported: " + invalidRequest.getIdentifier());

    verify(communicator).receiveMessage(MSG_LEN);
    verify(communicator).close();
  }

  @Test
  void shouldProcessTrafficIngress() {
    when(communicator.receiveMessage(MSG_LEN))
        .thenReturn(new TrafficIngressRequestDto())
        .thenReturn(new TrafficIngressRequestDto())
        .thenReturn(new CloseEntityDto());
    doNothing().when(communicator).close();

    final ArgumentCaptor<Dto> responseCaptor = ArgumentCaptor.forClass(Dto.class);
    doNothing().when(communicator).sendMessage(responseCaptor.capture());

    userEquipment.run();

    for (int i = 0; i < responseCaptor.getAllValues().size(); i++) {
      verifyTrafficIngress((TrafficIngressResponseDto) responseCaptor.getAllValues().get(i), i + 1);
      verify(communicator).sendMessage(responseCaptor.getAllValues().get(i));
    }

    verify(communicator, times(responseCaptor.getAllValues().size() + 1)).receiveMessage(MSG_LEN);
    verify(communicator).close();
  }
}
