package communication.model;

import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import java.io.IOException;
import lombok.Getter;
import org.msgpack.core.MessageBufferPacker;
import types.BsStateType;

@Getter
public class NewStateRequestDto extends Dto {

  private final BsStateType state;

  public NewStateRequestDto(BsStateType state) {
    super(DtoIdentifier.NEW_STATE_REQUEST);
    this.state = state;
  }

  @Override
  protected void map(final MessageBufferPacker messageBufferPacker) throws IOException {
    messageBufferPacker.packByte(state.getValue());
  }
}
