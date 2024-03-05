package task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import distribution.Distribution;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TaskGeneratorTest {

  @Test
  void shouldGenerateTask() {
    final Distribution sizeDist = Mockito.mock(Distribution.class);
    final Distribution delayDist = Mockito.mock(Distribution.class);

    final double size = Instancio.create(Double.class);
    final double delay = Instancio.create(Double.class);

    when(sizeDist.getRandom()).thenReturn(size);
    when(delayDist.getRandom()).thenReturn(delay);

    final Task task = new TaskGenerator(sizeDist, delayDist).generateTask();

    assertThat(task).isNotNull();
    assertThat(task.size()).isEqualTo(size);
    assertThat(task.tUntilNextTask()).isEqualTo(delay);
  }
}
