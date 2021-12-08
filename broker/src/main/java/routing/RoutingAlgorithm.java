package routing;

import entities.Bs;
import entities.Ue;

import java.util.Map;

public record RoutingAlgorithm(RoutingAlgorithmMode mode) {

    private static double distanceVector(double xUe, double yUe, double xBs, double yBs) {
        final double leg1 = xUe - xBs;
        final double leg2 = yUe - yBs;
        return Math.sqrt(leg1 * leg1 + leg2 * leg2);
    }

    public Bs getBs(Ue ue, Map<Integer, Bs> listBs) {

        Bs bs = null;
        double distanceMin = -1;
        double distance = 0;

        for (var entry : listBs.entrySet()) {
            final Bs bsAux = entry.getValue();
            switch (mode) {
                case DISTANCE_VECTOR -> distance = distanceVector(ue.getX(), ue.getY(), bsAux.getX(), bsAux.getY());
            }
            if (distance < distanceMin || bs == null) {
                distanceMin = distance;
                bs = bsAux;
            }
        }

        return bs;
    }
}
