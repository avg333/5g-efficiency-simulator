package communication.model;

import static org.instancio.Select.field;

import communication.model.base.BaseDtoTest;
import communication.model.base.Dto;
import java.io.IOException;
import java.util.List;
import org.instancio.Instancio;
import org.msgpack.core.MessageUnpacker;

class RegisterResponseDtoTest extends BaseDtoTest {

  @Override
  protected List<RegisterResponseDto> createDtos() {
    return Instancio.ofList(RegisterResponseDto.class)
        .set(field(Dto::getIdentifier), RegisterResponseDto.IDENTIFIER)
        .create();
  }

  @Override
  protected Dto createResult(final MessageUnpacker messageUnpacker) throws IOException {
    return new RegisterResponseDto(messageUnpacker);
  }

  @Override
  protected int offSet() {
    return 2;
  }
}
