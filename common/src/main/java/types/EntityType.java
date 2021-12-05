package types;

public enum EntityType {
    USER_EQUIPMENT(1), BASE_STATION(2), BROKER(3), UNADMITTED(0);

    public final int value;

    EntityType(final int value) {
        this.value = value;
    }

    public static EntityType getCommunicatorTypeTypeByCode(int code) {
        for (EntityType e : EntityType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }
}
