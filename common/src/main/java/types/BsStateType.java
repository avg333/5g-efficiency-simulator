package types;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BsStateType {
  ON((byte) 1),
  OFF((byte) 2),
  TO_ON((byte) 3),
  TO_OFF((byte) 4),
  HYSTERESIS((byte) -1),
  WAITING_TO_ON((byte) -2);

  private final byte value;

  private static final Map<Byte, BsStateType> BY_VALUE_MAP =
      Stream.of(values()).collect(Collectors.toMap(BsStateType::getValue, stateType -> stateType));

  public static BsStateType getStateTypeByCode(final byte code) {
    final BsStateType result = BY_VALUE_MAP.get(code);
    if (result == null) {
      throw new IllegalArgumentException("Value " + code + " not supported for the bs state type");
    }
    return result;
  }
}
