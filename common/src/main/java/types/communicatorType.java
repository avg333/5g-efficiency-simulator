package types;

public enum communicatorType {
    USER_EQUIPMENT(1), BASE_STATION(2), UNADMITTED(0);

    private final int value;

    communicatorType(final int value) {
        this.value = value;
    }

    public static communicatorType getCommunicatorTypeTypeByCode(int code) {
        for (communicatorType e : communicatorType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }

    public static int getCodeByCommunicatorType(communicatorType action) {
        for (communicatorType e : communicatorType.values()) {
            if (action == e) return e.value;
        }
        return 0;
    }
}
