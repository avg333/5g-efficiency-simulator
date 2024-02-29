package communication;

import communication.model.base.Dto;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterServerTCP extends RegisterServer {

  private static final String LOCALHOST = "localhost";

  public RegisterServerTCP(int port) {
    super(port);
  }

  @Override
  public void runServer() {
    try (final ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        final Socket clientSocket = serverSocket.accept();
        final ClientCommunicator communicator = new ClientCommunicatorTCP(clientSocket);
        final Dto dto = communicator.receiveMessage(MSG_LEN);

        if (processDto(dto, communicator)) {
          return;
        }
      }
    } catch (Exception e) {
      log.error("Log server error.. Execution completed", e);
      System.exit(-1);
    }
  }

  @Override
  protected void sendCloseMsgToServer(final Dto dto) {
    new ClientCommunicatorTCP(LOCALHOST, port).sendMessage(dto);
  }

  protected final void close() {
    // The TCP server closes automatically after complete the register of the entities
  }
}
