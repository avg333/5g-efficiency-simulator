package communication;

import org.msgpack.core.MessageUnpacker;
import types.EventType;
import types.StateType;

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
    private StateType stateReceived;
    private StateType state;
    private StateType nextState;

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

    public Message(StateType stateReceived) {
        this.stateReceived = stateReceived;
    }

    public Message(double tTrafficEgress, double tNewState, types.StateType stateReceived, types.StateType nextState, double q) {
        this.tTrafficEgress = tTrafficEgress;
        this.tNewState = tNewState;
        this.stateReceived = stateReceived;
        this.nextState = nextState;
        this.q = q;
    }

    public Message(double size, double q, double tTrafficEgress, double tNewState, types.StateType state, types.StateType nextState, double w, long id) {
        this.size = size;
        this.q = q;
        this.tTrafficEgress = tTrafficEgress;
        this.tNewState = tNewState;
        this.state = state;
        this.nextState = nextState;
        this.w = w;
        this.id = id;
    }

    public EventType getAction() {
        return action;
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
            case TRAFFIC_EGRESS -> {
                t = message.unpackDouble();
            }
            case NEW_STATE -> {
                final int stateReceivedInt = message.unpackInt();
                stateReceived = StateType.getStateTypeByCode(stateReceivedInt);
            }
        }
        message.close();
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

    public StateType getStateReceived() {
        return stateReceived;
    }

    public StateType getState() {
        return state;
    }

    public StateType getNextState() {
        return nextState;
    }
}
