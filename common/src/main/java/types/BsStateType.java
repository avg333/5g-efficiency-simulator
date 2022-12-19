package types;

public enum BsStateType {
    ON(1), OFF(2), TO_ON(3), TO_OFF(4), HYSTERESIS(-1), WAITING_TO_ON(-2);

    public final int value;

    BsStateType(final int value) {
        this.value = value;
    }

    public static BsStateType getStateTypeByCode(int code) {
        for (BsStateType e : BsStateType.values()) {
            if (code == e.value) return e;
        }
        throw new IllegalArgumentException("Value " + code + " not supported for the bs state type");
    }
}