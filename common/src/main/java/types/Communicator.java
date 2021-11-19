package types;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Communicator {
    private static final Logger LOGGER = Logger.getLogger(Communicator.class.getName());

    private communicatorType type;
    private DatagramSocket sc;
    private DatagramPacket dp;

    public Communicator(final communicatorType type, final String ipBroker, final int portBroker, final int id, final double x, final double y) {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(communicatorType.getCodeByCommunicatorType(type)).packInt(id).packDouble(x).packDouble(y).close();
            final byte[] message = packer.toByteArray();
            final InetAddress ad = InetAddress.getByName(ipBroker);
            dp = new DatagramPacket(message, message.length, ad, portBroker);
            sc = new DatagramSocket();
            sc.send(dp);
        } catch (IOException ex) {
            final String msg = "Registration failed. Execution completed";
            LOGGER.log(Level.SEVERE, msg, ex);
            System.exit(-1);
        }

    }

    public void close() {
        try {
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public actionType receiveActionType() {
        try {
            final byte[] data = new byte[10];
            sc.receive(new DatagramPacket(data, data.length));
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
            final int action = unpacker.unpackInt();
            unpacker.close();
            return actionType.getActionTypeByCode(action);
        } catch (IOException ex) {
            final String msg = "Error trying to receive a message. Execution completed";
            LOGGER.log(Level.SEVERE, msg);
            System.exit(-1);
        }

        return actionType.UNADMITTED;
    }

    public void sendTask(Double x, Double y, Double size, Double delay) {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packDouble(x).packDouble(y).packDouble(size).packDouble(delay).close();
            final byte[] message = packer.toByteArray();
            dp.setData(message, 0, message.length);
            sc.send(dp);
        } catch (IOException ex) {
            final String msg = "Error trying to send a message. Execution completed";
            LOGGER.log(Level.SEVERE, msg);
            System.exit(-1);
        }
    }

    public MessageUnpacker receiveMessage() {
        MessageUnpacker unpacker = null;
        try {
            final byte[] data = new byte[50];
            sc.receive(new DatagramPacket(data, data.length));
            unpacker = MessagePack.newDefaultUnpacker(data);
        } catch (IOException ex) {
            final String msg = "Error trying to receive a message. Execution completed";
            LOGGER.log(Level.SEVERE, msg);
            System.exit(-1);
        }

        return unpacker;
    }

    public void sendMessage(final MessageBufferPacker packer) {
        try {
            final byte[] message = packer.toByteArray();
            dp.setData(message, 0, message.length);
            sc.send(dp);
        } catch (IOException ex) {
            final String msg = "Error trying to receive a message. Execution completed";
            LOGGER.log(Level.SEVERE, msg);
            System.exit(-1);
        }
    }
}
