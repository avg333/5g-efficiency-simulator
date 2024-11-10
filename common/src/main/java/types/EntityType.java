package types;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityType {
  USER_EQUIPMENT(1),
  BASE_STATION(2),
  BROKER(3);

  private final int value;

  public static EntityType fromValue(final int value) {
    return Arrays.stream(values())
        .filter(communicatorType -> communicatorType.getValue() == value)
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + value + " not supported for the communicator type"));
  }
}
