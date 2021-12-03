package algorithm;

public enum AlgorithmMode {
    NO_COALESCING('N'), SIZE_BASED_COALESCING('S'), TIME_BASED_COALESCING('T'), FIXED_COALESCING('F');

    private final char value;

    AlgorithmMode(final char value) {
        this.value = value;
    }

    public static AlgorithmMode getModeTypeByCode(final char code) {
        for (AlgorithmMode e : AlgorithmMode.values()) {
            if (code == e.value) return e;
        }
        return null;
    }
}
