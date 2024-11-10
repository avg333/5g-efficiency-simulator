package task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import distribution.Distribution;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskGeneratorTest {

  private TaskGenerator taskGenerator;

  @Mock private Distribution sizeDist;
  @Mock private Distribution delayDist;

  @BeforeEach
  void setUp() {
    taskGenerator = new TaskGenerator(sizeDist, delayDist);
  }

  @Test
  void shouldGenerateTaskSuccessfully() {
    final double size = Instancio.create(Double.class);
    final double delay = Instancio.create(Double.class);

    when(sizeDist.getRandom()).thenReturn(size);
    when(delayDist.getRandom()).thenReturn(delay);

    final Task task = taskGenerator.generateTask();
    assertThat(task).isNotNull();
    assertThat(task.size()).isEqualTo(size);
    assertThat(task.tUntilNextTask()).isEqualTo(delay);
  }
}
