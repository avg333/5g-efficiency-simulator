package communication;

import static java.util.Objects.nonNull;

import exception.CommunicatorCreationException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommunicatorUDP extends Communicator {
  private final int portBroker;
  private final DatagramSocket sc;
  private final InetAddress ad;

  public CommunicatorUDP(final DatagramSocket sc, final InetAddress ad, final int portBroker) {
    this.sc = sc;
    this.ad = ad;
    this.portBroker = portBroker;
  }

  public CommunicatorUDP(final String ipBroker, final int portBroker) {
    this.portBroker = portBroker;
    try {
      this.sc = new DatagramSocket();
      sc.setSoTimeout(TIMEOUT);
      this.ad = InetAddress.getByName(ipBroker);
    } catch (IOException e) {
      log.error("Error trying to create the communicator", e);
      this.close();
      throw new CommunicatorCreationException(e);
    }
  }

  @Override
  protected final void send(final byte[] message) throws IOException {
    sc.send(new DatagramPacket(message, message.length, ad, portBroker));
  }

  @Override
  protected final byte[] receive(final int dataLen) throws IOException {
    final byte[] data = new byte[dataLen];
    sc.receive(new DatagramPacket(data, data.length));
    return data;
  }

  @Override
  public final void close() {
    if (nonNull(sc) && !sc.isClosed()) {
      try {
        sc.close();
      } catch (Exception e) {
        log.error("Error trying to close the socket", e);
      }
    }
  }

  @Override
  public String toString() {
    return "ad=" + ad + ", port=" + portBroker;
  }
}
