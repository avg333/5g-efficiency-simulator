package basestation;

import static communication.model.base.DtoIdentifier.TRAFFIC_ARRIVAL_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import algorithm.AlgorithmMode;
import communication.Communicator;
import communication.model.TrafficIngressRequestDto;
import domain.Position;
import exception.NotSupportedActionException;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseStationTest {

  private static final int MSG_LEN = TRAFFIC_ARRIVAL_REQUEST.getSize();

  @Mock private Communicator communicator;

  private BaseStation baseStation;

  @BeforeEach
  void setUp() {
    final BaseStationConfig baseStationConfig =
        new BaseStationConfig(AlgorithmMode.NO_COALESCING, 0.0, 0.0, 0.0, 0.0, 0.0);
    this.baseStation =
        new BaseStation(Instancio.create(Position.class), communicator, baseStationConfig);
  }

  @Test
  void shouldReturnMsgLen() {
    assertThat(baseStation.getMsgLen()).isEqualTo(MSG_LEN);
  }

  @Test
  void shouldThrowNotSupportedActionExceptionWhenUserEquipmentProcessReceivesAction() {
    final TrafficIngressRequestDto invalidRequest =
        Instancio.create(TrafficIngressRequestDto.class);
    when(communicator.receiveMessage(MSG_LEN)).thenReturn(invalidRequest);
    doNothing().when(communicator).close();

    assertThatThrownBy(() -> baseStation.run())
        .isInstanceOf(NotSupportedActionException.class)
        .hasMessageContaining("Action not supported: " + invalidRequest.getIdentifier());

    verify(communicator).receiveMessage(MSG_LEN);
    verify(communicator).close();
  }
}
