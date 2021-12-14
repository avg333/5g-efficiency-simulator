package routing;

import entities.Bs;
import entities.Ue;

import java.util.Map;

public record RoutingAlgorithm(RoutingAlgorithmMode mode) {

    public Bs getBs(final Ue ue, final Map<Integer, Bs> listBs) {
        Bs bs = null;

        switch (mode) {
            case DISTANCE_VECTOR -> {
                bs = distanceVector(ue, listBs);
            }
        }

        return bs;
    }

    private static Bs distanceVector(final Ue ue, final Map<Integer, Bs> listBs) {
        Bs bs = null;
        double distanceMin = -1;
        double distance;
        for (var entry : listBs.entrySet()) {
            final Bs bsAux = entry.getValue();
            distance = getDistance(ue.getX(), ue.getY(), bsAux.getX(), bsAux.getY());

            if (distance < distanceMin || bs == null) {
                distanceMin = distance;
                bs = bsAux;
            }
        }
        return bs;
    }

    private static double getDistance(double xUe, double yUe, double xBs, double yBs) {
        final double leg1 = xUe - xBs;
        final double leg2 = yUe - yBs;
        return Math.sqrt(leg1 * leg1 + leg2 * leg2);
    }
}
