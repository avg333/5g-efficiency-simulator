package types;

public enum stateType {
    ON(1), OFF(2), TO_ON(3), TO_OFF(4), HISTERISIS(-1), WAITING_TO_ON(-2), UNADMITTED(0);

    private final int value;

    stateType(final int value) {
        this.value = value;
    }

    public static stateType getStateTypeByCode(int code) {
        for (stateType e : stateType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }

    public static int getCodeByStateType(stateType state) {
        for (stateType e : stateType.values()) {
            if (state == e) return e.value;
        }
        return 0;
    }
}