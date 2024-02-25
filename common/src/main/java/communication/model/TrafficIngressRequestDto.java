package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

public class TrafficIngressRequestDto extends Dto {

  public TrafficIngressRequestDto() {
    super(DtoIdentifier.TRAFFIC_INGRESS_REQUEST);
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    // No data to pack
  }
}
