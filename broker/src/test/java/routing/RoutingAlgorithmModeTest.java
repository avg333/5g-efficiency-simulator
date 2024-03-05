package routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RoutingAlgorithmModeTest {

  @ParameterizedTest
  @EnumSource(RoutingAlgorithmMode.class)
  void shouldGetDistributionModeByCodeSuccessfully(final RoutingAlgorithmMode mode) {
    assertThat(RoutingAlgorithmMode.fromCode(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void testGetDistributionModeByCodeUnsupported() {
    final String unsupportedCode = Instancio.create(String.class);
    assertThrows(
        IllegalArgumentException.class, () -> RoutingAlgorithmMode.fromCode(unsupportedCode));
  }
}
