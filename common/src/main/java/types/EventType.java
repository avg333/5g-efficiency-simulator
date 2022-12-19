package types;

public enum EventType {
    TRAFFIC_INGRESS(1), TRAFFIC_ROUTE(2), TRAFFIC_ARRIVE(3), TRAFFIC_EGRESS(4), NEW_STATE(5), CLOSE(-1);

    public final int value;

    EventType(final int value) {
        this.value = value;
    }

    public static EventType getActionTypeByCode(int code) {
        for (EventType e : EventType.values()) {
            if (code == e.value) return e;
        }
        throw new IllegalArgumentException("Value " + code + " not supported for the event type");
    }
}
