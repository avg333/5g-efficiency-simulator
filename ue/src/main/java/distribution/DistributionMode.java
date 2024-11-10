package distribution;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DistributionMode {
  DETERMINISTIC("d"),
  UNIFORM("u"),
  EXPONENTIAL("e");

  private final String value;

  public static DistributionMode fromValue(final String value) {
    return Arrays.stream(values())
        .filter(distributionMode -> distributionMode.value.equals(value))
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + value + " not supported for the distribution type"));
  }
}
