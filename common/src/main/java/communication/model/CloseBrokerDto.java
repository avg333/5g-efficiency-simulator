package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

public class CloseBrokerDto extends Dto {

  public CloseBrokerDto() {
    super(DtoIdentifier.CLOSE_BROKER);
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    // No data to pack
  }
}
