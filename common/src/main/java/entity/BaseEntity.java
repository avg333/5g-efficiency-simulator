package entity;

import communication.Communicator;
import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import domain.Position;
import exception.NotSupportedActionException;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import types.EntityType;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseEntity implements Runnable {

  protected final Position position;

  private final Communicator communicator;

  protected static int getMaxMsgLen(final DtoIdentifier... actions) {
    return Stream.of(actions).mapToInt(DtoIdentifier::getSize).max().orElse(0);
  }

  protected abstract int getMsgLen();

  protected abstract EntityType getEntityType();

  protected abstract void processAction(Dto dto);

  @Override
  public final void run() {
    communicator.register(getEntityType(), position);

    while (true) {
      final Dto dto = receiveAction();

      final DtoIdentifier action = dto.getIdentifier();
      log.debug("Received request for {}", action);

      if (action == DtoIdentifier.CLOSE_ENTITY) {
        break;
      }

      processAction(dto);
    }

    communicator.close();
    log.info("Execution completed");
  }

  private Dto receiveAction() {
    return communicator.receiveMessage(getMsgLen());
  }

  protected final void sendMessage(final Dto dto) {
    communicator.sendMessage(dto);
  }

  protected final void processNotSupportedAction(final Dto dto) {
    log.error("Type {} not supported. Execution completed", dto.getIdentifier());
    communicator.close();
    throw new NotSupportedActionException(dto);
  }
}
