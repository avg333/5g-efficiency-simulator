package utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Utils {

  public static void closeResource(final AutoCloseable resource, final String resourceName) {
    if (resource != null) {
      try {
        resource.close();
      } catch (Exception e) {
        log.error("Error trying to close the " + resourceName + ".", e);
      }
    }
  }
}
