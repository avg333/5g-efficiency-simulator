package task;

import distribution.Distribution;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskGenerator {

  private final Distribution sizeDist;

  private final Distribution delayDist;

  public Task generateTask() {
    return new Task(sizeDist.getRandom(), delayDist.getRandom());
  }
}
