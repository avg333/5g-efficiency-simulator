package routing;

public enum RoutingAlgorithmMode {
    DISTANCE_VECTOR('V'), UNADMITTED('x');

    private final char value;

    RoutingAlgorithmMode(final char value) {
        this.value = value;
    }

    public static RoutingAlgorithmMode getRoutingAlgorithmModeTypeByCode(final char code) {
        for (RoutingAlgorithmMode e : RoutingAlgorithmMode.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }
}
