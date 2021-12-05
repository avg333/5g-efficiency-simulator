package routing;

public enum RoutingAlgorithmMode {
    DISTANCE_VECTOR('v');

    private final char value;

    RoutingAlgorithmMode(final char value) {
        this.value = value;
    }

    public static RoutingAlgorithmMode getRoutingAlgorithmModeTypeByCode(final char code) {
        for (RoutingAlgorithmMode e : RoutingAlgorithmMode.values()) {
            if (code == e.value) return e;
        }
        throw new IllegalArgumentException("Value " + code + " not supported for the routing algorithm");
    }
}
