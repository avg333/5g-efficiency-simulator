package communication;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.EntityType;

import java.io.IOException;
import java.net.InetAddress;

public abstract class Communicator {
    static final int TIMEOUT = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(Communicator.class);

    void register(EntityType type, InetAddress ad, int portBroker, double x, double y, MessageBufferPacker packer) throws IOException {
        LOGGER.debug("Trying to register the {} with the host {} in the port {}", type, ad, portBroker);
        packer.packInt(type.value);
        packer.packDouble(x);
        packer.packDouble(y);
        this.sendMessage(packer);
        final MessageUnpacker messageUnpacker = this.receiveMessage(10);
        final int id = messageUnpacker.unpackInt();
        messageUnpacker.close();
        LOGGER.debug("Registered the {} with id {}", type, id);
    }

    public abstract MessageUnpacker receiveMessage(int dataLen);

    public abstract void sendMessage(MessageBufferPacker packer);

    public abstract void close();
}
