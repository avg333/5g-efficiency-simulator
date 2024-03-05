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

  public static CommunicatorMode fromCode(final String code) {
    return Arrays.stream(CommunicatorMode.values())
        .filter(e -> e.value.equals(code))
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the communicator type"));
  }
}
