package communication;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

public interface Communicator {

    MessageUnpacker receiveMessage(int dataLen);

    void sendMessage(MessageBufferPacker packer);

    void close();
}
