package communication;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunicatorMode {
  TCP('t'),
  UDP('u');

  private final char value;

  public static CommunicatorMode fromCode(final char code) {
    return Arrays.stream(CommunicatorMode.values())
        .filter(e -> e.value == code)
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the communicator type"));
  }
}
