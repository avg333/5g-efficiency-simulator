package userequipment;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.Communicator;
import types.CommunicatorType;
import types.EventType;

public class CommunicatorUE extends Communicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicatorUE.class);

    private static final int MSG_LEN = 10;

    public CommunicatorUE(CommunicatorType type, String ipBroker, int portBroker, double x, double y) {
        super(type, ipBroker, portBroker, x, y);
    }

    public EventType receiveActionType() {
        try (final MessageUnpacker unpacker = this.receiveMessage(MSG_LEN)) {
            final int action = unpacker.unpackInt();
            return EventType.getActionTypeByCode(action);
        } catch (Exception e) {
            LOGGER.error("Error unpacking the message. Execution completed", e);
            this.close();
            System.exit(-1);
        }
        return EventType.UNADMITTED;
    }

    public void sendTask(final Double x, final Double y, final Double size, final Double delay) {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packDouble(x);
            packer.packDouble(y);
            packer.packDouble(size);
            packer.packDouble(delay);
            this.sendMessage(packer);
        } catch (Exception e) {
            LOGGER.error("Error packing the message. Execution completed", e);
            this.close();
            System.exit(-1);
        }
    }
}
