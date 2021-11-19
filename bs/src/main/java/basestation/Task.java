package basestation;

public class Task {
    private static double tLastArrive = 0.0;

    private long id;
    private double size;
    private double tArrive;

    public Task(long id, double size, double tArrive) {
        this.id = id;
        this.size = size;
        this.tArrive = tArrive;
    }

    public static double gettLastArrive() {
        return tLastArrive;
    }

    public static void settLastArrive(double tLastArrive) {
        Task.tLastArrive = tLastArrive;
    }

    public static double getDelay(double tArrive) {
        final double delay = tArrive - tLastArrive;
        tLastArrive = tArrive;
        return delay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double gettArrive() {
        return tArrive;
    }

    public void settArrive(double tArrive) {
        this.tArrive = tArrive;
    }
}
