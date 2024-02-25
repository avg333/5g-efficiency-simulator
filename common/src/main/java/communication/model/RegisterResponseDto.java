package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;

@Getter
public class RegisterResponseDto extends Dto {

  private final int id;

  public RegisterResponseDto(int id) {
    super(DtoIdentifier.REGISTER_RESPONSE);
    this.id = id;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packInt(id);
  }
}
