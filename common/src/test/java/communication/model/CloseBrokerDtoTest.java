package communication.model;

import static org.instancio.Select.field;

import communication.model.base.BaseDtoTest;
import communication.model.base.Dto;
import java.util.List;
import org.instancio.Instancio;
import org.msgpack.core.MessageUnpacker;

class CloseBrokerDtoTest extends BaseDtoTest {

  @Override
  protected List<CloseBrokerDto> createDtos() {
    return Instancio.ofList(CloseBrokerDto.class)
        .set(field(Dto::getIdentifier), CloseBrokerDto.IDENTIFIER)
        .create();
  }

  @Override
  protected Dto createResult(final MessageUnpacker messageUnpacker) {
    return new CloseBrokerDto();
  }
}
