package types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BsStateTypeTest {

  @ParameterizedTest
  @EnumSource(BsStateType.class)
  void shouldGetBsStateTypeByValueSuccessfully(final BsStateType mode) {
    assertThat(BsStateType.fromValue(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenBsStateTypeIsObtainedFromWrongValue() {
    final byte unsupportedValue = (byte) 10;
    assertThatThrownBy(() -> BsStateType.fromValue(unsupportedValue))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(String.valueOf(unsupportedValue));
  }
}
