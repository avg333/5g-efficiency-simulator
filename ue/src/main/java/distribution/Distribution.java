package distribution;

import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class Distribution {

  private final Random rand = new Random();
  private final DistributionMode distributionMode;
  private final double param1;
  private final double param2;

  public void setSeed(final long seed) {
    rand.setSeed(seed);
  }

  public double getRandom() {
    return switch (distributionMode) {
      case DETERMINISTIC -> param1;
      case UNIFORM -> rand.nextDouble() * (param1 - param2) + param2;
      case EXPONENTIAL ->
          param1 != 0
              ? Math.log(1 - rand.nextDouble()) / (-param1)
              : throwExceptionWhenDivisionByZero();
    };
  }

  private double throwExceptionWhenDivisionByZero() {
    throw new IllegalArgumentException("param1 can not be zero for exponential distribution");
  }
}
