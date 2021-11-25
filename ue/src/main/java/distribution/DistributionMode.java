package distribution;

public enum DistributionMode {
    DETERMINISTIC('d'), UNIFORM('u'), EXPONENTIAL('e'), UNADMITTED('x');

    private final char value;

    DistributionMode(final char value) {
        this.value = value;
    }

    public static DistributionMode getDistributionModeByCode(char code) {
        for (DistributionMode e : DistributionMode.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }
}