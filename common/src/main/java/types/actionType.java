package types;

public enum actionType {
    TRAFFIC_INGRESS(1), TRAFFIC_ARRIVE(2), TRAFFIC_EGRESS(3), NEW_STATE(4), CLOSE(-1), UNADMITTED(0);

    private final int value;

    actionType(final int value) {
        this.value = value;
    }

    public static actionType getActionTypeByCode(int code) {
        for (actionType e : actionType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }

    public static int getCodeByActionType(actionType action) {
        for (actionType e : actionType.values()) {
            if (action == e) return e.value;
        }
        return 0;
    }
}
