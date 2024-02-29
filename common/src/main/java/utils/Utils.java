package utils;

import static java.util.Objects.nonNull;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Utils {

  public static void closeResource(final AutoCloseable resource, final String resourceName) {
    if (nonNull(resource)) {
      try {
        resource.close();
      } catch (final Exception e) {
        log.error("Error trying to close the " + resourceName + ".", e);
      }
    }
  }
}
