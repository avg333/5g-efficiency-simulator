package routing;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoutingAlgorithmMode {
  DISTANCE_VECTOR("v");

  private final String value;

  public static RoutingAlgorithmMode fromCode(final String code) {
    return Arrays.stream(RoutingAlgorithmMode.values())
        .filter(e -> e.value.equals(code))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the routing algorithm"));
  }
}
