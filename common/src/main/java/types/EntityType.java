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

  public static EntityType getCommunicatorTypeTypeByCode(final int code) {
    return Arrays.stream(values())
        .filter(communicatorType -> communicatorType.getValue() == code)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Value " + code + " not supported for the communicator type"));
  }
}
