package routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RoutingAlgorithmModeTest {

  @ParameterizedTest
  @EnumSource(RoutingAlgorithmMode.class)
  void shouldGetRoutingAlgorithmModeByCodeSuccessfully(final RoutingAlgorithmMode mode) {
    assertThat(RoutingAlgorithmMode.fromValue(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenRoutingAlgorithmModeIsObtainedFromWrongValue() {
    final String unsupportedValue = Instancio.create(String.class);
    assertThatThrownBy(() -> RoutingAlgorithmMode.fromValue(unsupportedValue))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(unsupportedValue);
  }
}
