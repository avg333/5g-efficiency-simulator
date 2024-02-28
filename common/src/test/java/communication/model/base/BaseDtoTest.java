package communication.model.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

@Slf4j
public abstract class BaseDtoTest {

  @Test
  protected final void shouldMapAndUnmapSuccessfully() throws IOException {
    final Dto source = createDto();

    final MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(source.toByteArray());

    assertThat(DtoIdentifier.fromCode(messageUnpacker.unpackByte()))
        .isEqualTo(source.getIdentifier());
    assertThat(createResult(messageUnpacker))
        .isNotNull()
        .isInstanceOf(source.getClass())
        .usingRecursiveComparison()
        .isEqualTo(source);
    messageUnpacker.close();
  }

  @Test
  protected final void checkSize() throws IOException {
    final Dto source = createDto();
    final int expectedSize =
        DtoIdentifier.fromCode(source.getIdentifier().getCode()).getSize();

    assertThat(source.toByteArray())
        .isNotNull()
        .hasSizeBetween(expectedSize - offSet(), expectedSize + offSet());
  }

  protected int offSet() {
    return 0;
  }

  protected abstract Dto createDto();

  protected abstract Dto createResult(MessageUnpacker messageUnpacker) throws IOException;
}
