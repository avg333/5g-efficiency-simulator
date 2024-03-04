package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import org.msgpack.core.MessageBufferPacker;

public class TrafficIngressRequestDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.TRAFFIC_INGRESS_REQUEST;

  public TrafficIngressRequestDto() {
    super(IDENTIFIER);
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) {
    // No data to pack
  }
}
