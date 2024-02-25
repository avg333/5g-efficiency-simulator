package distribution;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DistributionMode {
  DETERMINISTIC('d'),
  UNIFORM('u'),
  EXPONENTIAL('e');

  private final char value;

  public static DistributionMode getDistributionModeByCode(final char code) {
    return Arrays.stream(DistributionMode.values())
        .filter(e -> e.value == code)
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the distribution type"));
  }
}
