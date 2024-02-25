package communication;

import static communication.model.base.DtoIdentifier.REGISTER_RESPONSE;

import communication.model.RegisterRequestDto;
import communication.model.RegisterResponseDto;
import communication.model.base.Dto;
import domain.Position;
import exception.MessageProcessingException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import types.EntityType;

@Slf4j
public abstract class Communicator {
  private static final int REGISTER_RESPONSE_SIZE = REGISTER_RESPONSE.getSize();

  private final DtoFactory dtoFactory = new DtoFactory();
  protected static final int TIMEOUT = 0;

  public final void register(final EntityType type, final Position position) {
    log.debug("Trying to register the {}", type);
    final RegisterResponseDto registerResponseDto =
        (RegisterResponseDto)
            communicate(new RegisterRequestDto(type, position), REGISTER_RESPONSE_SIZE);
    log.debug("Registered the {} with id {}", type, registerResponseDto.getId());
  }

  public final Dto communicate(final Dto dto, final int dataLen) {
    this.sendMessage(dto);
    return this.receiveMessage(dataLen);
  }

  public final Dto receiveMessage(final int dataLen) {
    try {
      return dtoFactory.createDto(receive(dataLen));
    } catch (IOException e) {
      log.error("Error trying to receive a message", e);
      this.close();
      throw new MessageProcessingException("Error trying to receive a message", e);
    }
  }

  public final void sendMessage(final Dto dto) {
    try {
      send(dto.toByteArray());
    } catch (IOException e) {
      log.error("Error trying to send a message", e);
      this.close();
      throw new MessageProcessingException("Error trying to send a message", e);
    }
  }

  protected abstract void send(byte[] message) throws IOException;

  protected abstract byte[] receive(int dataLen) throws IOException;

  public abstract void close();
}
