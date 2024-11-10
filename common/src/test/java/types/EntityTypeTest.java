package types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EntityTypeTest {

  @ParameterizedTest
  @EnumSource(EntityType.class)
  void shouldGetEntityTypeByValueSuccessfully(final EntityType mode) {
    assertThat(EntityType.fromValue(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenEntityTypeIsObtainedFromWrongValue() {
    final int unsupportedValue = 0;
    assertThatThrownBy(() -> EntityType.fromValue(unsupportedValue))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(String.valueOf(unsupportedValue));
  }
}
