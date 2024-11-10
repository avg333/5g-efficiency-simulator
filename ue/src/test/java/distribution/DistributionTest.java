package distribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DistributionTest {

  private static final int NUM_TESTS = 1000000;
  private static final long SEED = 3000;
  private static final double DELTA = 0.001;

  private Distribution distribution;

  @ParameterizedTest
  @EnumSource(DistributionMode.class)
  void shouldSetSeedSuccessfully(DistributionMode distributionMode) {
    final long seed = Instancio.create(Long.class);
    final double param1 = Instancio.create(Double.class);
    final double param2 = Instancio.create(Double.class);
    final Distribution distribution1 = new Distribution(distributionMode, param1, param2);
    distribution1.setSeed(seed);
    final Distribution distribution2 = new Distribution(distributionMode, param1, param2);
    distribution2.setSeed(seed);
    assertThat(distribution1.getRandom()).isEqualTo(distribution2.getRandom());
  }

  @Test
  void getRandomReturnsParam1WhenDistributionModeIsDeterministic() {
    final double param1 = Instancio.create(Double.class);
    distribution =
        new Distribution(DistributionMode.DETERMINISTIC, param1, Instancio.create(Double.class));
    assertThat(distribution.getRandom()).isEqualTo(param1);
  }

  @Test
  void getRandomReturnsValueInRangeWhenDistributionModeIsUniform() {
    final double param1 = 1.0;
    final double param2 = 2.0;
    distribution = new Distribution(DistributionMode.UNIFORM, param1, param2);
    distribution.setSeed(SEED);

    double sum = 0.0;
    for (int i = 0; i < NUM_TESTS; i++) {
      final double randomValue = distribution.getRandom();
      assertThat(randomValue).isBetween(Math.min(param1, param2), Math.max(param1, param2));
      sum += randomValue;
    }
    double average = sum / NUM_TESTS;
    double expectedAverage = (param1 + param2) / 2;

    assertThat(average).isCloseTo(expectedAverage, within(DELTA));
  }

  @Test
  void getRandomReturnsNonNegativeValueWhenDistributionModeIsExponential() {
    final double param1 = 1.0;
    final double param2 = 2.0;
    distribution = new Distribution(DistributionMode.EXPONENTIAL, param1, param2);
    distribution.setSeed(SEED);

    double sum = 0.0;
    for (int i = 0; i < NUM_TESTS; i++) {
      final double randomValue = distribution.getRandom();
      assertThat(randomValue).isNotNegative();
      sum += randomValue;
    }
    double average = sum / NUM_TESTS;
    double expectedAverage = 1 / param1;

    assertThat(average).isCloseTo(expectedAverage, within(DELTA));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenParam1IsZeroForExponentialDistribution() {
    distribution =
        new Distribution(DistributionMode.EXPONENTIAL, 0.0, Instancio.create(Double.class));
    assertThatThrownBy(distribution::getRandom)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("param1 can not be zero for exponential distribution");
  }
}
