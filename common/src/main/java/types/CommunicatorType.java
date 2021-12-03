package types;

public enum CommunicatorType {
    USER_EQUIPMENT(1), BASE_STATION(2), BROKER(3), UNADMITTED(0);

    private final int value;

    CommunicatorType(final int value) {
        this.value = value;
    }

    public static CommunicatorType getCommunicatorTypeTypeByCode(int code) {
        for (CommunicatorType e : CommunicatorType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }

    public static int getCodeByCommunicatorType(CommunicatorType action) {
        for (CommunicatorType e : CommunicatorType.values()) {
            if (action == e) return e.value;
        }
        return 0;
    }
}
