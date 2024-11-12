package communication.model.base;

import java.io.IOException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

@Getter
@RequiredArgsConstructor
public abstract class Dto {

  private final DtoIdentifier identifier;

  protected abstract void map(MessageBufferPacker messageBufferPacker) throws IOException;

  public final byte[] toByteArray() throws IOException {
    try (final MessageBufferPacker messageBufferPacker = MessagePack.newDefaultBufferPacker()) {
      messageBufferPacker.packByte(identifier.getValue());
      map(messageBufferPacker);
      return messageBufferPacker.toByteArray();
    }
  }
}
