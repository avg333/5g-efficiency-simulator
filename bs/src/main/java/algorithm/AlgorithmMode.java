package algorithm;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlgorithmMode {
  NO_COALESCING('n'),
  SIZE_BASED_COALESCING('s'),
  TIME_BASED_COALESCING('t'),
  FIXED_COALESCING('f');

  private final char value;

  public static AlgorithmMode getModeTypeByCode(final char code) {
    return Arrays.stream(AlgorithmMode.values())
        .filter(e -> e.value == code)
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the algorithm mode"));
  }
}
