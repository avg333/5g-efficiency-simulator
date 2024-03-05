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

  public static DistributionMode fromCode(final String code) {
    return Arrays.stream(DistributionMode.values())
        .filter(e -> e.value.equals(code))
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the distribution type"));
  }
}
