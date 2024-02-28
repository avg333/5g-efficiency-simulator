package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

public class CloseBrokerDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.CLOSE_BROKER;

  public CloseBrokerDto() {
    super(IDENTIFIER);
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    // No data to pack
  }
}
