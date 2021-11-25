package communication;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.CommunicatorType;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CommunicatorUDP implements Communicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicatorUDP.class);
    private static final int TIMEOUT = 0;
    private final int portBroker;
    private DatagramSocket sc;
    private InetAddress ad;

    public CommunicatorUDP(final CommunicatorType type, final String ipBroker, final int portBroker, final double x, final double y) {
        this.portBroker = portBroker;

        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            this.sc = new DatagramSocket();
            sc.setSoTimeout(TIMEOUT);
            this.ad = InetAddress.getByName(ipBroker);
            LOGGER.debug("Trying to register the {} with the host {} in the port {}", type, ad, portBroker);
            packer.packInt(CommunicatorType.getCodeByCommunicatorType(type));
            packer.packDouble(x);
            packer.packDouble(y);
            this.sendMessage(packer);
            final MessageUnpacker unpacker = this.receiveMessage(10);
            final int id = unpacker.unpackInt();
            unpacker.close();
            LOGGER.debug("Registered the {} with id {}", type, id);
        } catch (Exception e) {
            LOGGER.error("Registration failed. Execution completed", e);
            System.exit(-1);
        }

    }

    public CommunicatorUDP(final DatagramSocket sc, final InetAddress ad, final int portBroker) {
        this.sc = sc;
        this.ad = ad;
        this.portBroker = portBroker;
    }

    @Override
    public MessageUnpacker receiveMessage(final int dataLen) {
        try {
            final byte[] data = new byte[dataLen];
            sc.receive(new DatagramPacket(data, data.length));
            return MessagePack.newDefaultUnpacker(data);
        } catch (Exception e) {
            LOGGER.error("Error trying to receive a message. Execution completed", e);
            this.close();
            System.exit(-1);
        }

        return null;
    }

    @Override
    public void sendMessage(final MessageBufferPacker packer) {
        try {
            packer.close();
            final byte[] message = packer.toByteArray();
            final DatagramPacket dp = new DatagramPacket(message, message.length, ad, portBroker);
            sc.send(dp);
        } catch (Exception e) {
            LOGGER.error("Error trying to send a message. Execution completed", e);
            this.close();
            System.exit(-1);
        }
    }

    @Override
    public void close() {
        if (sc != null && !sc.isClosed()) {
            try {
                sc.close();
            } catch (Exception e) {
                LOGGER.error("Error trying to close the socket. Execution completed", e);
                System.exit(-1);
            }
        }

    }

    @Override
    public String toString() {
        return "ad=" + ad + ", portBroker=" + portBroker;
    }
}
