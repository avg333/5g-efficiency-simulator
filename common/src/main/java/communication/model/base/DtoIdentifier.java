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
  TRAFFIC_INGRESS_RESPONSE((byte) 3, 33),
  REGISTER_REQUEST((byte) 4, 21),
  REGISTER_RESPONSE((byte) 5, 5),
  TRAFFIC_ARRIVAL_RESPONSE((byte) 6, 27),
  TRAFFIC_ARRIVAL_REQUEST((byte) 7, 25),
  TRAFFIC_EGRESS_RESPONSE((byte) 8, 34),
  TRAFFIC_EGRESS_REQUEST((byte) 9, 9),
  NEW_STATE_RESPONSE((byte) 10, 19),
  NEW_STATE_REQUEST((byte) 11, 2),
  ;

  private final byte code;
  private final int size;

  private static final Map<Byte, DtoIdentifier> BY_CODE_MAP =
      Stream.of(values())
          .collect(Collectors.toMap(DtoIdentifier::getCode, identifier -> identifier));

  public static DtoIdentifier getDtoIdentifierByCode(final byte code) {
    final DtoIdentifier result = BY_CODE_MAP.get(code);
    if (result == null) {
      throw new IllegalArgumentException("Unknown dto identifier");
    }
    return result;
  }
}
