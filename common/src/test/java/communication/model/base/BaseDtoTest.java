package communication.model.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

public abstract class BaseDtoTest {

  @Test
  protected final void shouldMapAndUnmapSuccessfully() throws IOException {
    for (final Dto dto : createDtos()) {
      try (final MessageUnpacker messageUnpacker =
          MessagePack.newDefaultUnpacker(dto.toByteArray())) {

        assertThat(DtoIdentifier.fromCode(messageUnpacker.unpackByte()))
            .isEqualTo(dto.getIdentifier());
        assertThat(createResult(messageUnpacker))
            .isNotNull()
            .isInstanceOf(dto.getClass())
            .usingRecursiveComparison()
            .isEqualTo(dto);
      }
    }
  }

  @Test
  protected final void checkSize() throws IOException {
    for (final Dto dto : createDtos()) {
      final int expectedSize = DtoIdentifier.fromCode(dto.getIdentifier().getCode()).getSize();

      assertThat(dto.toByteArray())
          .isNotNull()
          .hasSizeBetween(expectedSize - offSet(), expectedSize + offSet());
    }
  }

  protected int offSet() {
    return 0;
  }

  protected abstract List<? extends Dto> createDtos();

  protected abstract Dto createResult(MessageUnpacker messageUnpacker) throws IOException;
}
