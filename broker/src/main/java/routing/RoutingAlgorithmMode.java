package routing;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoutingAlgorithmMode {
  DISTANCE_VECTOR('v');

  private final char value;

  public static RoutingAlgorithmMode getRoutingAlgorithmModeTypeByCode(final char code) {
    return Arrays.stream(RoutingAlgorithmMode.values())
        .filter(e -> e.value == code)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the routing algorithm"));
  }
}
