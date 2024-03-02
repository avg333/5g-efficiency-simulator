package communication.model;

import static org.instancio.Select.field;

import communication.model.base.BaseDtoTest;
import communication.model.base.Dto;
import java.io.IOException;
import java.util.List;
import org.instancio.Instancio;
import org.msgpack.core.MessageUnpacker;

class TrafficIngressResponseDtoTest extends BaseDtoTest {

  @Override
  protected List<TrafficIngressResponseDto> createDtos() {
    return Instancio.ofList(TrafficIngressResponseDto.class)
        .set(field(Dto::getIdentifier), TrafficIngressResponseDto.IDENTIFIER)
        .create();
  }

  @Override
  protected Dto createResult(final MessageUnpacker messageUnpacker) throws IOException {
    return new TrafficIngressResponseDto(messageUnpacker);
  }
}
