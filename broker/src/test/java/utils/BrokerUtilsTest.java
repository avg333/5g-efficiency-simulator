package utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

class BrokerUtilsTest {

  @Test
  void shouldReturnFormattedFileName() {
    final String name = Instancio.create(String.class);
    final String extension = Instancio.create(String.class);
    final String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

    String result = BrokerUtils.getFileName(name, extension);

    assertThat(result).isEqualTo(name + "_" + dateStr + "." + extension);
  }
}
