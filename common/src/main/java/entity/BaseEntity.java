package entity;

import communication.ClientCommunicator;
import communication.model.RegisterRequestDto;
import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import domain.Position;
import exception.NotSupportedActionException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import types.EntityType;

@Slf4j
public abstract class BaseEntity implements Runnable {

  private final ClientCommunicator communicator;
  protected final Position position;

  protected BaseEntity(ClientCommunicator communicator, Position position) {
    this.communicator = communicator;
    this.position = position;
  }

  protected static int getMaxMsgLen(final DtoIdentifier... actions) {
    return Arrays.stream(actions).mapToInt(DtoIdentifier::getSize).max().orElse(0);
  }

  protected abstract int getMsgLen();

  protected abstract EntityType getEntityType();

  protected abstract Dto processAction(Dto dto);

  @Override
  public final void run() {
    communicator.register(
        new RegisterRequestDto(getEntityType(), position.getX(), position.getY()));

    try {
      while (true) {
        final Dto dto = communicator.receiveMessage(getMsgLen());

        final DtoIdentifier action = dto.getIdentifier();
        log.debug("Received request for {}", action);

        if (action == DtoIdentifier.CLOSE_ENTITY) {
          break;
        }

        communicator.sendMessage(processAction(dto));
      }

    } catch (Exception e) {
      log.error("", e);
      // TODO Throw KO
      throw e;
    } finally {
      communicator.close();
    }

    log.info("Execution completed");
  }

  protected final Dto processNotSupportedAction(final Dto dto) {
    log.error("Type {} not supported. Execution completed", dto.getIdentifier());
    throw new NotSupportedActionException(dto);
  }
}
