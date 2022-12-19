package algorithm;

public enum AlgorithmMode {
    NO_COALESCING('n'), SIZE_BASED_COALESCING('s'), TIME_BASED_COALESCING('t'), FIXED_COALESCING('f');

    private final char value;

    AlgorithmMode(final char value) {
        this.value = value;
    }

    public static AlgorithmMode getModeTypeByCode(final char code) {
        for (AlgorithmMode e : AlgorithmMode.values()) {
            if (code == e.value) return e;
        }
        throw new IllegalArgumentException("Value " + code + " not supported for the algorithm mode");
    }
}
