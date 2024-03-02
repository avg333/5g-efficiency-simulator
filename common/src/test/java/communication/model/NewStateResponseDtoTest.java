package communication.model;

import static types.Constants.NO_NEXT_STATE;
import static types.Constants.NO_TASK_TO_PROCESS;

import communication.model.base.BaseDtoTest;
import communication.model.base.Dto;
import java.io.IOException;
import java.util.List;
import org.instancio.Instancio;
import org.msgpack.core.MessageUnpacker;
import types.BsStateType;

class NewStateResponseDtoTest extends BaseDtoTest {

  @Override
  protected List<NewStateResponseDto> createDtos() {
    return List.of(
        new NewStateResponseDto(
            Instancio.create(BsStateType.class),
            Instancio.create(BsStateType.class),
            NO_TASK_TO_PROCESS.getValue(),
            NO_NEXT_STATE.getValue(),
            Instancio.create(Double.class)),
        new NewStateResponseDto(
            Instancio.create(BsStateType.class),
            Instancio.create(BsStateType.class),
            Instancio.create(Double.class),
            NO_NEXT_STATE.getValue(),
            Instancio.create(Double.class)),
        new NewStateResponseDto(
            Instancio.create(BsStateType.class),
            Instancio.create(BsStateType.class),
            NO_TASK_TO_PROCESS.getValue(),
            Instancio.create(Double.class),
            Instancio.create(Double.class)),
        new NewStateResponseDto(
            Instancio.create(BsStateType.class),
            Instancio.create(BsStateType.class),
            Instancio.create(Double.class),
            Instancio.create(Double.class),
            Instancio.create(Double.class)));
  }

  @Override
  protected Dto createResult(final MessageUnpacker messageUnpacker) throws IOException {
    return new NewStateResponseDto(messageUnpacker);
  }

  @Override
  protected int offSet() {
    return 9;
  }
}
