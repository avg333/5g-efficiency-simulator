package communication;

import org.msgpack.core.MessageUnpacker;
import types.BsStateType;
import types.EventType;

import java.io.IOException;

public class Message {
    private EventType action;
    private double t;
    private long id;
    private double size;
    private BsStateType stateReceived;

    public Message() {

    }

    public Message(final MessageUnpacker message) throws IOException {
        final int type = message.unpackInt();
        action = EventType.getActionTypeByCode(type);
        switch (action) {
            case TRAFFIC_ARRIVE -> {
                t = message.unpackDouble();
                id = message.unpackLong();
                size = message.unpackDouble();
            }
            case TRAFFIC_EGRESS -> t = message.unpackDouble();
            case NEW_STATE -> {
                final int stateReceivedInt = message.unpackInt();
                stateReceived = BsStateType.getStateTypeByCode(stateReceivedInt);
            }
        }
        message.close();
    }

    public EventType getAction() {
        return action;
    }

    public double getT() {
        return t;
    }

    public long getId() {
        return id;
    }

    public double getSize() {
        return size;
    }

    public BsStateType getStateReceived() {
        return stateReceived;
    }
}
