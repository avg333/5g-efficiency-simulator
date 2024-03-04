package entity;

import communication.BaseClient;
import communication.ClientCommunicator;
import communication.model.RegisterRequestDto;
import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import domain.Position;
import exception.NotSupportedActionException;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import types.EntityType;

@Slf4j
public abstract class BaseEntity extends BaseClient implements Runnable {

  protected final Position position;

  protected BaseEntity(ClientCommunicator communicator, Position position) {
    super(communicator);
    this.position = position;
  }

  protected static int getMaxMsgLen(final DtoIdentifier... actions) {
    return Stream.of(actions).mapToInt(DtoIdentifier::getSize).max().orElse(0);
  }

  protected abstract int getMsgLen();

  protected abstract EntityType getEntityType();

  protected abstract Dto processAction(Dto dto);

  @Override
  public final void run() {
    register(new RegisterRequestDto(getEntityType(), position.getX(), position.getY()));

    while (true) {
      final Dto dto = receiveMessage(getMsgLen());

      final DtoIdentifier action = dto.getIdentifier();
      log.debug("Received request for {}", action);

      if (action == DtoIdentifier.CLOSE_ENTITY) {
        break;
      }

      sendMessage(processAction(dto));
    }

    close();
    log.info("Execution completed");
  }

  protected final Dto processNotSupportedAction(final Dto dto) {
    log.error("Type {} not supported. Execution completed", dto.getIdentifier());
    close();
    throw new NotSupportedActionException(dto);
  }
}
