package algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AlgorithmModeTest {

  @ParameterizedTest
  @EnumSource(AlgorithmMode.class)
  void shouldGetAlgorithmModeByValueSuccessfully(final AlgorithmMode mode) {
    assertThat(AlgorithmMode.fromValue(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenAlgorithmModeIsObtainedFromWrongValue() {
    final String unsupportedValue = Instancio.create(String.class);
    assertThatThrownBy(() -> AlgorithmMode.fromValue(unsupportedValue))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(unsupportedValue);
  }
}
