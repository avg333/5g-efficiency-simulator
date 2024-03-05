package domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

class PositionTest {

  @Test
  void shouldUpdateCoordinatesWhenMoved() {
    final Position position = Instancio.create(Position.class);

    final double initialX = position.getX();
    final double initialY = position.getY();
    final double deltaX = Instancio.create(double.class);
    final double deltaY = Instancio.create(double.class);

    position.move(deltaX, deltaY);

    assertThat(position.getX()).isEqualTo(initialX + deltaX);
    assertThat(position.getY()).isEqualTo(initialY + deltaY);
  }
}
