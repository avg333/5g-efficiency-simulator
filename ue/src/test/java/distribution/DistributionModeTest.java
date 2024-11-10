package distribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DistributionModeTest {

  @ParameterizedTest
  @EnumSource(DistributionMode.class)
  void shouldGetDistributionModeByCodeSuccessfully(final DistributionMode mode) {
    assertThat(DistributionMode.fromValue(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenDistributionModeIsObtainedFromWrongValue() {
    final String unsupportedValue = Instancio.create(String.class);
    assertThatThrownBy(() -> DistributionMode.fromValue(unsupportedValue))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(unsupportedValue);
  }
}
