package communication;

import communication.model.CloseBrokerDto;
import communication.model.RegisterRequestDto;
import communication.model.RegisterResponseDto;
import communication.model.base.Dto;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class ClientCommunicatorTCPTest {

  private static final int RANDOM_FREE_PORT = 0;
  private static final String LOCALHOST = "localhost";

  private ServerSocket serverSocket;
  private ClientCommunicatorTCP client;

  @BeforeEach
  void setUp() throws IOException {
    serverSocket = new ServerSocket(RANDOM_FREE_PORT);
    new Thread(
            () -> {
              try {
                setupClientInServer();
              } catch (IOException e) {
                log.error("Error in setupClientInServer", e);
              }
            })
        .start();
  }

  @AfterEach
  void tearDown() throws IOException {
    closeServerSocket();
  }

  @Test
  void testCommunicatorTCP() {
    client = new ClientCommunicatorTCP(LOCALHOST, serverSocket.getLocalPort());
    System.out.println("Testing CommunicatorTCP");
    client.sendMessage(new CloseBrokerDto());
    System.out.println("Tested CommunicatorTCP");
  }

  private void setupClientInServer() throws IOException {
    while (true) {
      final Socket clientSocket = serverSocket.accept();
      final BaseClientCommunicator communicator = new ClientCommunicatorTCP(clientSocket);
      final Dto dto = communicator.receiveMessage(21);

      if (dto instanceof CloseBrokerDto) {
        System.out.println("Closing server");
        return;
      }
      if (dto instanceof RegisterRequestDto) {
        System.out.println("Registering");
        final RegisterResponseDto registerResponseDto = new RegisterResponseDto(1);
        communicator.sendMessage(registerResponseDto);
        System.out.println("Registered");
      }
    }
  }

  private void closeServerSocket() throws IOException {
    try (final ClientCommunicator client =
        new ClientCommunicatorTCP(LOCALHOST, serverSocket.getLocalPort())) {
      client.sendMessage(new CloseBrokerDto());
    }
    serverSocket.close();
    client.close();
  }
}
