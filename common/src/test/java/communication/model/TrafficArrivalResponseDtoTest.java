package communication.model;

import static org.instancio.Select.field;

import communication.model.base.BaseDtoTest;
import communication.model.base.Dto;
import java.io.IOException;
import org.instancio.Instancio;
import org.msgpack.core.MessageUnpacker;

class TrafficArrivalResponseDtoTest extends BaseDtoTest {

  @Override
  protected Dto createDto() {
    return Instancio.of(TrafficArrivalResponseDto.class)
        .set(field(Dto::getIdentifier), TrafficArrivalResponseDto.IDENTIFIER)
        .create();
  }

  @Override
  protected Dto createResult(final MessageUnpacker messageUnpacker) throws IOException {
    return new TrafficArrivalResponseDto(messageUnpacker);
  }
}
