package types;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BsStateType {
  ON((byte) 1),
  OFF((byte) 2),
  TO_ON((byte) 3),
  TO_OFF((byte) 4),
  HYSTERESIS((byte) 5),
  WAITING_TO_ON((byte) 6);

  private static final Map<Byte, BsStateType> BY_VALUE_MAP =
      Arrays.stream(values())
          .collect(Collectors.toMap(BsStateType::getValue, stateType -> stateType));
  private final byte value;

  public static BsStateType fromCode(final byte code) {
    final BsStateType result = BY_VALUE_MAP.get(code);
    if (result == null) {
      throw new IllegalArgumentException("Value " + code + " not supported for the bs state type");
    }
    return result;
  }
}
