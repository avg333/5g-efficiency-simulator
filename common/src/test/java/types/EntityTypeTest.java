package types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EntityTypeTest {

  @ParameterizedTest
  @EnumSource(EntityType.class)
  void shouldGetDistributionModeByCodeSuccessfully(final EntityType mode) {
    assertThat(EntityType.fromCode(mode.getValue())).isEqualTo(mode);
  }

  @Test
  void testGetDistributionModeByCodeUnsupported() {
    final int unsupportedCode = Instancio.create(Integer.class);
    assertThrows(IllegalArgumentException.class, () -> EntityType.fromCode(unsupportedCode));
  }
}
