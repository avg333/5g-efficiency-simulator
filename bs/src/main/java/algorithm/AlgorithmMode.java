package algorithm;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlgorithmMode {
  NO_COALESCING("n"),
  SIZE_BASED_COALESCING("s"),
  TIME_BASED_COALESCING("t"),
  FIXED_COALESCING("f");

  private final String value;

  public static AlgorithmMode fromCode(final String code) {
    return Arrays.stream(AlgorithmMode.values())
        .filter(e -> e.value.equals(code))
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the algorithm mode"));
  }
}
