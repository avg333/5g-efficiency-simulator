package userequipment;

import distribution.Distribution;
import domain.Task;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskGenerator {

  private final Distribution sizeDist;

  private final Distribution delayDist;

  public Task generateTask() {
    return new Task(0, sizeDist.getRandom(), delayDist.getRandom());
  }
}
