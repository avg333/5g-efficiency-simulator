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
    private double q;
    private double tTrafficEgress;
    private double tNewState;
    private double w;
    private BsStateType stateReceived;
    private BsStateType state;
    private BsStateType nextState;

    public Message() {

    }

    public Message(int t, long id, double size) {
        this.t = t;
        this.id = id;
        this.size = size;
    }

    public Message(int t) {
        this.t = t;
    }

    public Message(BsStateType stateReceived) {
        this.stateReceived = stateReceived;
    }

    public Message(double tTrafficEgress, double tNewState, BsStateType stateReceived, BsStateType nextState, double q) {
        this.tTrafficEgress = tTrafficEgress;
        this.tNewState = tNewState;
        this.stateReceived = stateReceived;
        this.nextState = nextState;
        this.q = q;
    }

    public Message(double size, double q, double tTrafficEgress, double tNewState, BsStateType state, BsStateType nextState, double w, long id) {
        this.size = size;
        this.q = q;
        this.tTrafficEgress = tTrafficEgress;
        this.tNewState = tNewState;
        this.state = state;
        this.nextState = nextState;
        this.w = w;
        this.id = id;
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

    public double getQ() {
        return q;
    }

    public double gettTrafficEgress() {
        return tTrafficEgress;
    }

    public double gettNewState() {
        return tNewState;
    }

    public double getW() {
        return w;
    }

    public BsStateType getStateReceived() {
        return stateReceived;
    }

    public BsStateType getState() {
        return state;
    }

    public BsStateType getNextState() {
        return nextState;
    }
}
