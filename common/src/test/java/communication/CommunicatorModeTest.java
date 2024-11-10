package communication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CommunicatorModeTest {

  @ParameterizedTest
  @EnumSource(CommunicatorMode.class)
  void shouldGetCommunicatorModeByCodeSuccessfully(final CommunicatorMode mode) {
    assertThat(CommunicatorMode.fromValue(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenCommunicatorModeIsObtainedFromWrongValue() {
    final String unsupportedValue = Instancio.create(String.class);
    assertThatThrownBy(() -> CommunicatorMode.fromValue(unsupportedValue))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(unsupportedValue);
  }
}
