package utils;

import lombok.experimental.UtilityClass;
import task.Task;

@UtilityClass
public class BaseStationUtils {

  public static double calculateW(final double currentT, final Task task, final double c) {
    return currentT - task.tArrivalTime() - task.size() / c;
  }
}
