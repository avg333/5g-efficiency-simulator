package basestation;

enum algorithmMode {
    NO_COALESCING(1), SIZE_BASED_COALESCING(4), TIME_BASED_COALESCING(5), FIXED_COALESCING(-1), UNADMITTED(0);

    private final int value;

    algorithmMode(final int value) {
        this.value = value;
    }

    public static algorithmMode getModeTypeByCode(final char code) {
        for (algorithmMode e : algorithmMode.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }
}
