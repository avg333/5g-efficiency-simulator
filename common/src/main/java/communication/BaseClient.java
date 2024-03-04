package communication;

import communication.model.RegisterRequestDto;
import communication.model.base.Dto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseClient {

  private final ClientCommunicator communicator;

  protected final void register(final RegisterRequestDto dto) {
    communicator.register(dto);
  }

  protected final Dto receiveMessage(final int dataLen) {
    return communicator.receiveMessage(dataLen);
  }

  protected final void sendMessage(final Dto dto) {
    communicator.sendMessage(dto);
  }

  protected final void close() {
    communicator.close();
  }
}
