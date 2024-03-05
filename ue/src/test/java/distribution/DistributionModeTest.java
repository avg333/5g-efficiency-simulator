package distribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DistributionModeTest {

  @ParameterizedTest
  @EnumSource(DistributionMode.class)
  void shouldGetDistributionModeByCodeSuccessfully(final DistributionMode mode) {
    assertThat(DistributionMode.fromCode(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void testGetDistributionModeByCodeUnsupported() {
    final String unsupportedCode = Instancio.create(String.class);
    assertThrows(IllegalArgumentException.class, () -> DistributionMode.fromCode(unsupportedCode));
  }
}
