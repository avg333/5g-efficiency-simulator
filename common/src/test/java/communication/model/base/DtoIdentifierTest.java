package communication.model.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DtoIdentifierTest {

  @ParameterizedTest
  @EnumSource(DtoIdentifier.class)
  void shouldGetDtoIdentifierByValueSuccessfully(final DtoIdentifier mode) {
    assertThat(DtoIdentifier.fromValue(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenDtoIdentifierIsObtainedFromWrongValue() {
    final byte unsupportedValue = (byte) 12;
    assertThatThrownBy(() -> DtoIdentifier.fromValue(unsupportedValue))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(String.valueOf(unsupportedValue));
  }
}
