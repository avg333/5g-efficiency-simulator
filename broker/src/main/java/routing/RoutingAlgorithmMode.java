package routing;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoutingAlgorithmMode {
  DISTANCE_VECTOR("v");

  private final String value;

  public static RoutingAlgorithmMode fromValue(final String value) {
    return Arrays.stream(values())
        .filter(e -> e.value.equals(value))
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + value + " not supported for the routing algorithm"));
  }
}
