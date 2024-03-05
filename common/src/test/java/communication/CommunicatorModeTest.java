package communication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CommunicatorModeTest {

  @ParameterizedTest
  @EnumSource(CommunicatorMode.class)
  void shouldGetDistributionModeByCodeSuccessfully(final CommunicatorMode mode) {
    assertThat(CommunicatorMode.fromCode(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void testGetDistributionModeByCodeUnsupported() {
    final String unsupportedCode = Instancio.create(String.class);
    assertThrows(IllegalArgumentException.class, () -> CommunicatorMode.fromCode(unsupportedCode));
  }
}
