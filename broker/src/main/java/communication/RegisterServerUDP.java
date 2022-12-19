package communication;

import broker.Event;
import entities.Bs;
import entities.Ue;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class RegisterServerUDP extends RegisterServer {
    private static final int MSG_LEN = 50;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private DatagramSocket sc;

    public RegisterServerUDP(double t, int port, Map<Integer, Bs> listBs, Map<Integer, Ue> listUe, Map<Long, Event> listEvent) {
        super(t, port, listBs, listUe, listEvent);
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
                final Communicator communicator = new CommunicatorUDP(sc, ad, portEntity);
                final MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(dp.getData());

                final boolean closeRegister = processRegister(communicator, messageUnpacker);
                if (closeRegister) return;
            }
        } catch (Exception e) {
            log.error("Log server error. Execution completed", e);
            System.exit(-1);
        }
    }

    @Override
    public void closeRegisterServer(final MessageBufferPacker packer) {
        try (final DatagramSocket scAux = new DatagramSocket()) {
            packer.close();
            final byte[] message = packer.toByteArray();
            final InetAddress adR = InetAddress.getByName("localhost");
            final DatagramPacket dp = new DatagramPacket(message, message.length, adR, port);
            scAux.send(dp);
        } catch (IOException e) {
            log.error("Failed to shut down the log server. Execution completed", e);
            System.exit(-1);
        }
    }


    @Override
    void close() {
        try {
            if (sc != null && !sc.isClosed()) {
                sc.close();
            }
        } catch (Exception e) {
            log.error("Failed to shut down the server. Execution completed", e);
        }
    }

}
