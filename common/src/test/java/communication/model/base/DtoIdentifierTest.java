package communication.model.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DtoIdentifierTest {

  @ParameterizedTest
  @EnumSource(DtoIdentifier.class)
  void shouldGetDistributionModeByCodeSuccessfully(final DtoIdentifier mode) {
    assertThat(DtoIdentifier.fromCode(mode.getCode())).isEqualTo(mode);
  }

  @Test
  void testGetDistributionModeByCodeUnsupported() {
    final byte unsupportedCode = Instancio.create(Byte.class);
    assertThrows(IllegalArgumentException.class, () -> DtoIdentifier.fromCode(unsupportedCode));
  }
}
