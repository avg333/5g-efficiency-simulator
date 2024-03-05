package algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AlgorithmModeTest {

  @ParameterizedTest
  @EnumSource(AlgorithmMode.class)
  void shouldGetAlgorithmModeByCodeSuccessfully(final AlgorithmMode mode) {
    assertThat(AlgorithmMode.fromCode(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void testGetDistributionModeByCodeUnsupported() {
    final String unsupportedCode = Instancio.create(String.class);
    assertThrows(IllegalArgumentException.class, () -> AlgorithmMode.fromCode(unsupportedCode));
  }
}
