package communication;

import communication.model.RegisterRequestDto;
import communication.model.base.Dto;

public interface ClientCommunicator {

  void register(RegisterRequestDto dto);

  Dto communicate(Dto dto, int dataLen);

  Dto receiveMessage(int dataLen);

  void sendMessage(Dto dto);

  void close();
}
