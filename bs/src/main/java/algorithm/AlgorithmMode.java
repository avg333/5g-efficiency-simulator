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

  public static AlgorithmMode fromValue(final String value) {
    return Arrays.stream(AlgorithmMode.values())
        .filter(algorithmMode -> algorithmMode.value.equals(value))
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + value + " not supported for the algorithm mode"));
  }
}
