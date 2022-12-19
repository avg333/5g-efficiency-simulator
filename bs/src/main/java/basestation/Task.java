package basestation;

public record Task(long id, double size, double tArrive) {
    private static double tLastArrive = 0.0;

    public static double getDelay(double tArrive) {
        final double delay = tArrive - tLastArrive;
        tLastArrive = tArrive;
        return delay;
    }
}
