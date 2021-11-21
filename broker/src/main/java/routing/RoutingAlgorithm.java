package routing;

import entities.Bs;
import entities.Ue;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public record RoutingAlgorithm(RoutingAlgorithmMode mode) {

    private static final Logger LOGGER = Logger.getLogger(RoutingAlgorithm.class.getName());

    private static double distanceVector(double xUe, double yUe, double xBs, double yBs) {
        final double leg1 = xUe - xBs;
        final double leg2 = yUe - yBs;
        return Math.sqrt(leg1 * leg1 + leg2 * leg2);
    }

    public Bs getBs(Ue ue, Map<Integer, Bs> listaBS) {

        if (listaBS.isEmpty()) {
            final String msg = "Error: La lista de BS est� vac�a. Ejecuci�n finalizada";
            LOGGER.log(Level.SEVERE, msg);
            System.exit(-1);
        }

        Bs bs = null;
        double distanciaMin = -1;
        double distancia = 0;

        for (var entry : listaBS.entrySet()) {
            Bs bsAux = entry.getValue();
            switch (mode) {
                case DISTANCE_VECTOR -> distancia = distanceVector(ue.getX(), ue.getY(), bsAux.getX(), bsAux.getY());
            }
            if (distancia < distanciaMin || bs == null) {
                distanciaMin = distancia;
                bs = bsAux;
            }
        }

        return bs;
    }
}
