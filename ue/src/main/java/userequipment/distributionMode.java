package userequipment;

enum distributionMode {
    DETERMINISTIC('d'), UNIFORM('u'), EXPONENTIAL('e'), UNADMITTED('x');

    private final char value;

    distributionMode(final char value) {
        this.value = value;
    }

    public static distributionMode getDistributionModeByCode(char code) {
        for (distributionMode e : distributionMode.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }
}