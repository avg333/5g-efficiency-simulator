package communication;

import communication.model.RegisterRequestDto;
import communication.model.base.Dto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import types.EntityType;

@RequiredArgsConstructor
public abstract class BaseClient {

  @Getter private final EntityType type;
  private final ClientCommunicator communicator;

  protected final void register(RegisterRequestDto dto) {
    communicator.register(dto);
  }

  protected final Dto communicate(Dto dto, int dataLen) {
    return communicator.communicate(dto, dataLen);
  }

  protected final Dto receiveMessage(int dataLen) {
    return communicator.receiveMessage(dataLen);
  }

  protected final void sendMessage(Dto dto) {
    communicator.sendMessage(dto);
  }

  protected final void close() {
    communicator.close();
  }
}
