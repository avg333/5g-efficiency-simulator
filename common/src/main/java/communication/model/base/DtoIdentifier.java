package communication.model.base;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DtoIdentifier {
  REGISTER_REQUEST((byte) 0, 20),
  REGISTER_RESPONSE((byte) 1, 4),
  CLOSE_BROKER((byte) 2, 1),
  CLOSE_ENTITY((byte) 3, 1),
  // UE
  TRAFFIC_INGRESS_REQUEST((byte) 4, 1),
  TRAFFIC_INGRESS_RESPONSE((byte) 5, 37),
  // BS
  TRAFFIC_ARRIVAL_REQUEST((byte) 7, 22),
  TRAFFIC_ARRIVAL_RESPONSE((byte) 6, 41),
  TRAFFIC_EGRESS_REQUEST((byte) 9, 10),
  TRAFFIC_EGRESS_RESPONSE((byte) 8, 53),
  NEW_STATE_REQUEST((byte) 11, 2),
  NEW_STATE_RESPONSE((byte) 10, 32),
  ;

  private static final Map<Byte, DtoIdentifier> BY_CODE_MAP =
      Arrays.stream(values()).collect(Collectors.toMap(DtoIdentifier::getCode, id -> id));
  private final byte code;
  private final int size;

  public static DtoIdentifier fromCode(final byte code) {
    final DtoIdentifier result = BY_CODE_MAP.get(code);
    if (result == null) {
      throw new IllegalArgumentException("Unknown dto identifier code: " + code);
    }
    return result;
  }
}
