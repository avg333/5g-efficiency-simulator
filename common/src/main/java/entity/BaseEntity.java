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

@Slf4j
@RequiredArgsConstructor
public abstract class BaseEntity implements Runnable {

  protected final Position position;

  private final Communicator communicator;

  protected static int getMaxMsgLen(final DtoIdentifier... actions) {
    return Stream.of(actions).mapToInt(DtoIdentifier::getSize).max().orElse(0);
  }

  protected abstract int getMsgLen();

  protected abstract EntityType getEntityType();

  protected abstract Dto processAction(Dto dto);

  @Override
  public final void run() {
    communicator.register(getEntityType(), position);

    while (true) {
      final Dto dto = communicator.receiveMessage(getMsgLen());

      final DtoIdentifier action = dto.getIdentifier();
      log.debug("Received request for {}", action);

      if (action == DtoIdentifier.CLOSE_ENTITY) {
        break;
      }

      communicator.sendMessage(processAction(dto));
    }

    communicator.close();
    log.info("Execution completed");
  }

  protected final Dto processNotSupportedAction(final Dto dto) {
    log.error("Type {} not supported. Execution completed", dto.getIdentifier());
    communicator.close();
    throw new NotSupportedActionException(dto);
  }
}
