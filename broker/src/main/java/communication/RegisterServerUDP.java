package communication;

import static utils.Utils.closeResource;

import communication.model.base.Dto;
import communication.model.factory.DtoFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterServerUDP extends RegisterServer {

  private static final String LOCALHOST = "localhost";

  private final DtoFactory dtoFactory = new DtoFactory();

  private DatagramSocket sc;

  public RegisterServerUDP(int port) {
    super(port);
  }

  @Override
  public void runServer() {
    try {
      sc = new DatagramSocket(port);
      while (true) {
        final byte[] data = new byte[MSG_LEN];
        final DatagramPacket dp = new DatagramPacket(data, data.length);
        sc.receive(dp);
        final InetAddress ad = dp.getAddress();
        final int portEntity = dp.getPort();
        final BaseClientCommunicator communicator = new ClientCommunicatorUDP(sc, ad, portEntity);
        final Dto dto = dtoFactory.createDto(dp.getData());

        if (processDto(dto, communicator)) {
          return;
        }
      }
    } catch (Exception e) {
      log.error("Log server error. Execution completed", e);
      System.exit(-1);
    }
  }

  @Override
  protected void sendCloseMsgToServer(final Dto dto) {
    try (final ClientCommunicator clientCommunicator = new ClientCommunicatorUDP(LOCALHOST, port)) {
      clientCommunicator.sendMessage(dto);
    }
  }

  @Override
  protected final void close() {
    closeResource(sc, "DatagramSocket");
  }
}
