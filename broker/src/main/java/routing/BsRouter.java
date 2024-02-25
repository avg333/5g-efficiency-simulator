package routing;

import domain.Position;
import entities.Bs;
import entities.Ue;
import java.util.Comparator;
import java.util.List;

public record BsRouter(RoutingAlgorithmMode mode) {

  // TODO: Implement use parallel streams to improve performance can be an option
  private static Bs distanceVector(final Ue ue, final List<Bs> bsList) {
    return bsList.stream()
        .min(Comparator.comparingDouble(bs2 -> getDistance(ue.getPosition(), bs2.getPosition())))
        .orElseThrow(() -> new IllegalArgumentException("No base stations available"));
  }

  private static double getDistance(final Position positionUe, final Position positionBs) {
    final double leg1 = positionUe.getX() - positionBs.getX();
    final double leg2 = positionUe.getY() - positionBs.getY();
    return leg1 * leg1 + leg2 * leg2;
    // Technically, the distance is the square root of the sum of the squares of the legs
    // But we don't need to calculate the square root, because we only need to compare distances
  }

  public Bs getBs(final Ue ue, final List<Bs> bsList) {
    switch (mode) {
      case DISTANCE_VECTOR -> {
        return distanceVector(ue, bsList);
      }
      default -> throw new IllegalArgumentException("Invalid mode: " + mode);
    }
  }
}
