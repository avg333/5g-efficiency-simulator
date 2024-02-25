package distribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

class DistributionTest {

  private static final int NUM_TESTS = 1000;
  private static final double DELTA = 0.1;

  private Distribution distribution;

  @Test
  void getRandomReturnsParam1WhenDistributionModeIsDeterministic() {
    final double param1 = Instancio.create(Double.class);
    final double param2 = Instancio.create(Double.class);
    distribution = new Distribution(DistributionMode.DETERMINISTIC, param1, param2);

    for (int i = 0; i < NUM_TESTS; i++) {
      assertThat(distribution.getRandom()).isEqualTo(param1);
    }
  }

  @Test
  void getRandomReturnsValueInRangeWhenDistributionModeIsUniform() {
    final double param1 = 1.0;
    final double param2 = 2.0;
    distribution = new Distribution(DistributionMode.UNIFORM, param1, param2);

    double sum = 0.0;
    for (int i = 0; i < NUM_TESTS; i++) {
      double randomValue = distribution.getRandom();
      assertThat(randomValue).isBetween(param1, param2);
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

    double sum = 0.0;
    for (int i = 0; i < NUM_TESTS; i++) {
      double randomValue = distribution.getRandom();
      assertThat(randomValue).isNotNegative();
      sum += randomValue;
    }
    double average = sum / NUM_TESTS;
    double expectedAverage = 1 / param1;

    assertThat(average).isCloseTo(expectedAverage, within(DELTA));
  }

  @Test
  void getRandomReturnsZeroWhenDistributionModeIsExponentialAndParam1IsZero() {
    final double param1 = 0.0;
    final double param2 = Instancio.create(Double.class);
    distribution = new Distribution(DistributionMode.EXPONENTIAL, param1, param2);

    for (int i = 0; i < NUM_TESTS; i++) {
      assertThat(distribution.getRandom()).isZero();
    }
  }
}
