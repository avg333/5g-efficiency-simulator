package routing;

import domain.Position;
import domain.entities.Bs;
import domain.entities.Ue;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BsRouter {

  private final RoutingAlgorithmMode mode;

  private static Bs distanceVector(final Ue ue, final List<Bs> bsList) {
    // Implement use parallel streams to improve performance can be an option
    return bsList.stream()
        .min(Comparator.comparingDouble(bs2 -> getDistance(ue.getPosition(), bs2.getPosition())))
        .orElseThrow(() -> new IllegalArgumentException("No base stations available"));
  }

  private static double getDistance(final Position positionUe, final Position positionBs) {
    final double leg1 = positionUe.x() - positionBs.x();
    final double leg2 = positionUe.y() - positionBs.y();
    // Technically, the distance is the square root of the sum of the squares of the legs
    // But we don't need to calculate the square root, because we only need to compare distances
    return leg1 * leg1 + leg2 * leg2;
  }

  public Bs getBs(final Ue ue, final List<Bs> bsList) {
    return switch (mode) {
      case DISTANCE_VECTOR -> distanceVector(ue, bsList);
    };
  }
}
