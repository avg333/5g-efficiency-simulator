package communication.model.base;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DtoIdentifier {
  CLOSE_BROKER((byte) 0, 1),
  CLOSE_ENTITY((byte) 1, 1),
  TRAFFIC_INGRESS_REQUEST((byte) 2, 1),
  TRAFFIC_INGRESS_RESPONSE((byte) 3, 37), // 33 theoretically
  REGISTER_REQUEST((byte) 4, 20), // 21 theoretically
  REGISTER_RESPONSE((byte) 5, 4), // 5 theoretically
  TRAFFIC_ARRIVAL_RESPONSE((byte) 6, 39), // 27 theoretically
  TRAFFIC_ARRIVAL_REQUEST((byte) 7, 22), // 25 theoretically
  TRAFFIC_EGRESS_RESPONSE((byte) 8, 51), // 34 theoretically
  TRAFFIC_EGRESS_REQUEST((byte) 9, 10), // 9 theoretically
  NEW_STATE_RESPONSE((byte) 10, 30), // 19 theoretically
  NEW_STATE_REQUEST((byte) 11, 2);

  private static final Map<Byte, DtoIdentifier> BY_CODE_MAP =
      Stream.of(values())
          .collect(Collectors.toMap(DtoIdentifier::getCode, identifier -> identifier));
  private final byte code;
  private final int size;

  public static DtoIdentifier getDtoIdentifierByCode(final byte code) {
    final DtoIdentifier result = BY_CODE_MAP.get(code);
    if (result == null) {
      throw new IllegalArgumentException("Unknown dto identifier code: " + code);
    }
    return result;
  }
}
