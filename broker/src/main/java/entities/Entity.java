package entities;

import communication.Communicator;
import communication.model.CloseEntityDto;
import communication.model.base.Dto;
import domain.Position;
import lombok.Getter;
import lombok.Setter;

public abstract class Entity {

  private static int idCounter = 1;

  private final Communicator communicator;
  @Getter private final int id;

  @Getter
  @Setter(value = lombok.AccessLevel.PROTECTED)
  private Position position;

  Entity(Position position, Communicator communicator) {
    this.id = idCounter++;
    this.position = position;
    this.communicator = communicator;
  }

  public Dto communicate(final Dto dto, final int msgLen) {
    // TODO Obtains the response length from the request
    return communicator.communicate(dto, msgLen);
  }

  public void closeSocket() {
    communicator.sendMessage(new CloseEntityDto());
  }
}
