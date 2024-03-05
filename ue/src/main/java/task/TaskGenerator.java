package task;

import distribution.Distribution;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class TaskGenerator {

  private final Distribution sizeDist;
  private final Distribution delayDist;

  public final Task generateTask() {
    return new Task(sizeDist.getRandom(), delayDist.getRandom());
  }
}
