package communication;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunicatorMode {
  TCP("TCP"),
  UDP("UDP");

  private final String value;

  public static CommunicatorMode fromValue(final String value) {
    return Arrays.stream(values())
        .filter(e -> e.value.equals(value))
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + value + " not supported for the communicator type"));
  }
}
