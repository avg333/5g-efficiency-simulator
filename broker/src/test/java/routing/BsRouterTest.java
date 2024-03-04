package routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static routing.RoutingAlgorithmMode.DISTANCE_VECTOR;

import domain.Position;
import domain.entities.Bs;
import domain.entities.Ue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BsRouterTest {

  private final BsRouter bsRouter = new BsRouter(DISTANCE_VECTOR);

  @Test
  void testGetBs() {
    final Ue ue = Mockito.mock(Ue.class);
    final Bs bs1 = Mockito.mock(Bs.class);
    final Bs bs2 = Mockito.mock(Bs.class);
    final List<Bs> bsList = List.of(bs1, bs2);

    when(ue.getPosition()).thenReturn(new Position(0, 0));
    when(bs1.getPosition()).thenReturn(new Position(1, 1));
    when(bs2.getPosition()).thenReturn(new Position(2, 2));

    assertThat(bsRouter.getBs(ue, bsList)).isEqualTo(bs1);
  }

  @Test
  void testGetBsNoBaseStations() {
    final Ue ue = Mockito.mock(Ue.class);
    final List<Bs> emptyBsList = List.of();

    when(ue.getPosition()).thenReturn(new Position(0, 0));

    assertThrows(IllegalArgumentException.class, () -> bsRouter.getBs(ue, emptyBsList));
  }
}
