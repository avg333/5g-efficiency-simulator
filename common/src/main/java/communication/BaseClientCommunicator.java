package communication;

import static communication.model.base.DtoIdentifier.REGISTER_RESPONSE;

import communication.model.RegisterRequestDto;
import communication.model.RegisterResponseDto;
import communication.model.base.Dto;
import communication.model.factory.DtoFactory;
import exception.MessageProcessingException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseClientCommunicator implements ClientCommunicator {

  protected static final int TIMEOUT = 0;

  private static final int REGISTER_RESPONSE_SIZE = REGISTER_RESPONSE.getSize();

  private final DtoFactory dtoFactory = new DtoFactory();

  public final void register(final RegisterRequestDto dto) {
    log.debug("Trying to register the {}", dto.getType());
    final RegisterResponseDto registerResponseDto =
        (RegisterResponseDto) communicate(dto, REGISTER_RESPONSE_SIZE);
    log.debug("Registered the {} with id {}", dto.getType(), registerResponseDto.getId());
  }

  public final Dto communicate(final Dto dto, final int dataLen) {
    sendMessage(dto);
    return receiveMessage(dataLen);
  }

  public final Dto receiveMessage(final int dataLen) {
    try {
      return dtoFactory.createDto(receive(dataLen));
    } catch (final IOException e) {
      log.error("Error trying to receive a message", e);
      throw new MessageProcessingException("Error trying to receive a message", e);
    }
  }

  public final void sendMessage(final Dto dto) {
    try {
      send(dto.toByteArray());
    } catch (final IOException e) {
      log.error("Error trying to send a message", e);
      throw new MessageProcessingException("Error trying to send a message", e);
    }
  }

  protected abstract void send(byte[] message) throws IOException;

  protected abstract byte[] receive(int dataLen) throws IOException;
}
