package entities;

import communication.Communicator;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import types.EventType;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Entity {

    private static final Logger LOGGER = Logger.getLogger(Entity.class.getName());

    private static final int RESPONSE_MSG_LEN = 100;

    private final Communicator communicator;
    private double x;
    private double y;
    private int id;

    public Entity(double x, double y, Communicator communicator) {
        this.x = x;
        this.y = y;
        this.communicator = communicator;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void sendRegisterAck(final int id) {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(id);
            communicator.sendMessage(packer);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send the register ACK. Execution completed", e);
            System.exit(-1);
        }
    }

    public MessageUnpacker communicate(final MessageBufferPacker packer) {
        communicator.sendMessage(packer);
        return communicator.receiveMessage(RESPONSE_MSG_LEN);
    }

    public void closeSocket() {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(EventType.CLOSE.value);
            communicator.sendMessage(packer);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to close the socket. Execution completed", e);
            System.exit(-1);
        }
    }

}
