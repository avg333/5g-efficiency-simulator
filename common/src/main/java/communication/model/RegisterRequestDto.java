package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import domain.Position;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import types.EntityType;

@Getter
public class RegisterRequestDto extends Dto {

  private final EntityType type;

  private final Position position;

  public RegisterRequestDto(EntityType type, Position position) {
    super(DtoIdentifier.REGISTER_REQUEST);
    this.type = type;
    this.position = position;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packInt(type.getValue());
    messageBufferPacker.packDouble(position.getX());
    messageBufferPacker.packDouble(position.getY());
  }
}
