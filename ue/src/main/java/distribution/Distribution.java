package distribution;

import java.util.Random;

public class Distribution {
    private final Random rand = new Random();
    private final DistributionMode distributionMode;
    private final double param1;
    private final double param2;

    public Distribution(DistributionMode distributionMode, double param1, double param2) {
        this.distributionMode = distributionMode;
        this.param1 = param1;
        this.param2 = param2;
    }

    public void setSeed(long seed) {
        rand.setSeed(seed);
    }

    public double getRandom() {
        return switch (distributionMode) {
            case DETERMINISTIC -> param1;
            case UNIFORM -> rand.nextDouble() * (param1 - param2) + param2;
            case EXPONENTIAL -> param1 != 0 ? Math.log(1 - rand.nextDouble()) / (-param1) : 0;
        };
    }

    @Override
    public String toString() {
        return "mode=" + distributionMode + ", param1=" + param1 + ", param2=" + param2;
    }

}
