package communication.model;

import static org.instancio.Select.field;

import communication.model.base.BaseDtoTest;
import communication.model.base.Dto;
import java.io.IOException;
import org.instancio.Instancio;
import org.msgpack.core.MessageUnpacker;

class TrafficArrivalRequestDtoTest extends BaseDtoTest {

  @Override
  protected Dto createDto() {
    return Instancio.of(TrafficArrivalRequestDto.class)
        .set(field(Dto::getIdentifier), TrafficArrivalRequestDto.IDENTIFIER)
        .create();
  }

  @Override
  protected Dto createResult(final MessageUnpacker messageUnpacker) throws IOException {
    return new TrafficArrivalRequestDto(messageUnpacker);
  }

  @Override
  protected int offSet() {
    return 2;
  }
}
