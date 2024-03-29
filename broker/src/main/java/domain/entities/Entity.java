package domain.entities;

import communication.ClientCommunicator;
import communication.model.CloseEntityDto;
import communication.model.base.Dto;
import domain.Position;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Entity {

  private static int idCounter = 1;
  @Getter private final int id = idCounter++;

  private final ClientCommunicator communicator;

  @Getter protected Position position;

  public final Dto communicate(final Dto dto, final int msgLen) {
    // TODO Obtains the response length from the request
    return communicator.communicate(dto, msgLen);
  }

  public final void closeSocket() {
    communicator.sendMessage(new CloseEntityDto());
  }
}
