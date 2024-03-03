package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BrokerUtils {

  private static final String DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss";

  public static String getFileName(final String name, final String extension) {
    final String dateStr = new SimpleDateFormat(DATE_FORMAT).format(new Date());
    return name + "_" + dateStr + "." + extension;
  }
}
