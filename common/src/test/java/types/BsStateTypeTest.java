package types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BsStateTypeTest {

  @ParameterizedTest
  @EnumSource(BsStateType.class)
  void shouldGetDistributionModeByCodeSuccessfully(final BsStateType mode) {
    assertThat(BsStateType.fromCode(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void testGetDistributionModeByCodeUnsupported() {
    final byte unsupportedCode = Instancio.create(Byte.class);
    assertThrows(IllegalArgumentException.class, () -> BsStateType.fromCode(unsupportedCode));
  }
}
