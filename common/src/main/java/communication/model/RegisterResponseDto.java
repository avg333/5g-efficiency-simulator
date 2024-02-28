package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

@Getter
public class RegisterResponseDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.REGISTER_RESPONSE;

  private final int id;

  public RegisterResponseDto(final int id) {
    super(IDENTIFIER);
    this.id = id;
  }

  public RegisterResponseDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(messageUnpacker.unpackInt());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packInt(id);
  }
}
