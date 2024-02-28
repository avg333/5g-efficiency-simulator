package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import types.BsStateType;

@Getter
public class NewStateRequestDto extends Dto {

  public static final DtoIdentifier IDENTIFIER = DtoIdentifier.NEW_STATE_REQUEST;

  private final BsStateType state;

  public NewStateRequestDto(final BsStateType state) {
    super(IDENTIFIER);
    this.state = state;
  }

  public NewStateRequestDto(final MessageUnpacker messageUnpacker) throws IOException {
    this(BsStateType.fromCode(messageUnpacker.unpackByte()));
  }

  @Override
  protected final void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packByte(state.getValue());
  }
}
