package communication;

import static communication.model.base.DtoIdentifier.REGISTER_REQUEST;

import communication.model.CloseBrokerDto;
import communication.model.RegisterRequestDto;
import communication.model.RegisterResponseDto;
import communication.model.base.Dto;
import domain.Position;
import domain.entities.Bs;
import domain.entities.Entity;
import domain.entities.Ue;
import exception.MessageProcessingException;
import exception.NotSupportedActionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import types.EntityType;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class RegisterServer {
  protected static final int MSG_LEN = REGISTER_REQUEST.getSize();

  protected final int port;
  private final List<Entity> entities = new ArrayList<>();

  private static void waitForEnter() {
    try (final Scanner in = new Scanner(System.in)) {
      in.nextLine();
    }
  }

  public List<Entity> getEntities() {
    log.info("Registered entities:");
    new Thread(this::runServer).start();
    waitForEnter();
    closeRegisterServer(new CloseBrokerDto());
    return entities;
  }

  public void closeSockets() {
    entities.stream().parallel().forEach(Entity::closeSocket);
    close();
  }

  private void closeRegisterServer(final Dto dto) {
    try {
      sendCloseMsgToServer(dto);
    } catch (IOException e) {
      log.error("Failed to stop the register server", e);
      closeSockets();
      throw new MessageProcessingException("Failed to stop the register server", e);
    }
  }

  protected abstract void sendCloseMsgToServer(Dto dto) throws IOException;

  protected abstract void runServer();

  protected abstract void close();

  protected boolean processDto(final Dto dto, final ClientCommunicator communicator) {
    return switch (dto.getIdentifier()) {
      case CLOSE_BROKER -> true;
      case REGISTER_REQUEST -> processRegister(communicator, (RegisterRequestDto) dto);
      default -> {
        log.error("Invalid message received");
        throw new NotSupportedActionException(dto);
      }
    };
  }

  private boolean processRegister(
      ClientCommunicator communicator, RegisterRequestDto registerRequestDto) {
    final EntityType type = registerRequestDto.getType();

    switch (type) {
      case BROKER -> {
        // If the sender is the broker, the server should stop
        return true;
      }
      case USER_EQUIPMENT ->
          registerUe(
              new Position(registerRequestDto.getX(), registerRequestDto.getY()), communicator);
      case BASE_STATION ->
          registerBs(
              new Position(registerRequestDto.getX(), registerRequestDto.getY()), communicator);
    }

    return false;
  }

  private void registerBs(final Position position, final ClientCommunicator communicator) {
    final Bs bs = new Bs(communicator, position);
    entities.add(bs);
    communicator.sendMessage(new RegisterResponseDto(bs.getId()));
    log.info("BS [id={}] {}", bs.getId(), communicator);
  }

  private void registerUe(final Position position, final ClientCommunicator communicator) {
    final Ue ue = new Ue(communicator, position);
    entities.add(ue);
    communicator.sendMessage(new RegisterResponseDto(ue.getId()));
    log.info("UE [id={}] {}", ue.getId(), communicator);
  }
}
