package types;

public enum StateType {
    ON(1), OFF(2), TO_ON(3), TO_OFF(4), HYSTERESIS(-1), WAITING_TO_ON(-2), UNADMITTED(0);

    public final int value;

    StateType(final int value) {
        this.value = value;
    }

    public static StateType getStateTypeByCode(int code) {
        for (StateType e : StateType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }
}