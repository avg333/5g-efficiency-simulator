package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

public class CloseEntityDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.CLOSE_ENTITY;

  public CloseEntityDto() {
    super(IDENTIFIER);
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    // No data to pack
  }
}
