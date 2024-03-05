package utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import task.Task;

class BaseStationUtilsTest {

  @Test
  void testCalculateW() {
    final Task task = Instancio.create(Task.class);
    final double c = Instancio.create(Double.class);
    final double currentT = Instancio.create(Double.class);

    assertThat(BaseStationUtils.calculateW(currentT, task, c))
        .isEqualTo(currentT - task.tArrivalTime() - task.size() / c);
  }
}
