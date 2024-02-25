package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

public class CloseEntityDto extends Dto {

  public CloseEntityDto() {
    super(DtoIdentifier.CLOSE_ENTITY);
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    // No data to pack
  }
}
