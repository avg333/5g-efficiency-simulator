package entities;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import types.actionType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Entity {

    private static final Logger LOGGER = Logger.getLogger(Entity.class.getName());

    private static final int RESPONSE_MSG_LEN = 100;

    private final int id;
    private double x;
    private double y;
    private final DatagramSocket sc;
    private final InetAddress ad;
    private final int port;

    public Entity(int id, double x, double y, DatagramSocket sc, InetAddress ad, int port) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.sc = sc;
        this.ad = ad;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public MessageUnpacker communicate(MessageBufferPacker packer) {
        MessageUnpacker unpacker = null;
        try {
            final byte[] dataRequest = packer.toByteArray();
            final byte[] dataResponse = new byte[RESPONSE_MSG_LEN];
            sc.send(new DatagramPacket(dataRequest, dataRequest.length, ad, port));
            sc.receive(new DatagramPacket(dataResponse, dataResponse.length));
            unpacker = MessagePack.newDefaultUnpacker(dataResponse);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending / receiving a message. Execution completed", e);
            this.closeSocket();
            System.exit(-1);
        }

        return unpacker;
    }

    public void closeSocket() {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(actionType.getCodeByActionType(actionType.CLOSE)).close();
            final byte[] dataRequest = packer.toByteArray();
            sc.send(new DatagramPacket(dataRequest, dataRequest.length, ad, port));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to close the socket. Execution completed", e);
            System.exit(-1);
        }
    }

}
