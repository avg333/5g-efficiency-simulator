package types;

public enum CommunicatorType {
    USER_EQUIPMENT(1), BASE_STATION(2), BROKER(3), UNADMITTED(0);

    public final int value;

    CommunicatorType(final int value) {
        this.value = value;
    }

    public static CommunicatorType getCommunicatorTypeTypeByCode(int code) {
        for (CommunicatorType e : CommunicatorType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }
}
