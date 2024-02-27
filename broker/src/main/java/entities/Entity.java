package entities;

import communication.Communicator;
import communication.model.CloseEntityDto;
import communication.model.base.Dto;
import domain.Position;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Entity {

  private static int idCounter = 1;

  private final Communicator communicator;
  @Getter private final int id = idCounter++;

  @Getter protected Position position;

  public Dto communicate(final Dto dto, final int msgLen) {
    // TODO Obtains the response length from the request
    return communicator.communicate(dto, msgLen);
  }

  public void closeSocket() {
    communicator.sendMessage(new CloseEntityDto());
  }
}
