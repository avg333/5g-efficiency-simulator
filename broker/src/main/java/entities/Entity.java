package entities;

import communication.Communicator;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.EventType;

public abstract class Entity {

    private static final int RESPONSE_MSG_LEN = 100;
    private static int idCounter = 1;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Communicator communicator;
    private final int id;
    private double x;
    private double y;

    Entity(double x, double y, Communicator communicator) {
        this.id = idCounter++;
        this.x = x;
        this.y = y;
        this.communicator = communicator;
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

    public void sendRegisterAck(final int id) {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(id);
            communicator.sendMessage(packer);
        } catch (Exception e) {
            log.error("Failed to send the register ACK. Execution completed", e);
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
            log.error("Failed to close the socket. Execution completed", e);
            System.exit(-1);
        }
    }

}
