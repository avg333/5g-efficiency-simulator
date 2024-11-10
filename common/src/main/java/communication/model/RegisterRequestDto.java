package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import types.EntityType;

@Getter
public class RegisterRequestDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.REGISTER_REQUEST;

  private final EntityType type;

  private final double x;

  private final double y;

  public RegisterRequestDto(final EntityType type, final double x, final double y) {
    super(IDENTIFIER);
    this.type = type;
    this.x = x;
    this.y = y;
  }

  public RegisterRequestDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(
        EntityType.fromValue(messageUnpacker.unpackInt()),
        messageUnpacker.unpackDouble(),
        messageUnpacker.unpackDouble());
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packInt(type.getValue());
    messageBufferPacker.packDouble(x);
    messageBufferPacker.packDouble(y);
  }
}
