package basestation;

import static org.assertj.core.api.Assertions.assertThat;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class BaseStationConfigDtoTest {

  @Test
  void shouldParseCommandLineArguments() {
    String expectedBrokerIp = Instancio.create(String.class);
    int expectedBrokerPort = Instancio.create(Integer.class);
    String expectedCommunicatorMode = Instancio.create(String.class);
    double expectedPositionX = Instancio.create(Double.class);
    double expectedPositionY = Instancio.create(Double.class);
    int expectedProcessingCapacity = Instancio.create(Integer.class);
    String expectedAlgorithmMode = Instancio.create(String.class);
    int expectedAlgorithmParam = Instancio.create(Integer.class);
    int expectedTimeToOff = Instancio.create(Integer.class);
    int expectedTimeToOn = Instancio.create(Integer.class);
    int expectedTimeHysteresis = Instancio.create(Integer.class);

    final String[] args = {
      "-s", expectedBrokerIp,
      "-p", String.valueOf(expectedBrokerPort),
      "-m", expectedCommunicatorMode,
      "-x", String.valueOf(expectedPositionX),
      "-y", String.valueOf(expectedPositionY),
      "-c", String.valueOf(expectedProcessingCapacity),
      "-am", expectedAlgorithmMode,
      "-ap", String.valueOf(expectedAlgorithmParam),
      "-toff", String.valueOf(expectedTimeToOff),
      "-ton", String.valueOf(expectedTimeToOn),
      "-th", String.valueOf(expectedTimeHysteresis)
    };
    final BaseStationConfigDto config = new BaseStationConfigDto();
    new CommandLine(config).parseArgs(args);

    assertThat(config).isNotNull();
    assertThat(config.getBrokerIp()).isEqualTo(expectedBrokerIp);
    assertThat(config.getBrokerPort()).isEqualTo(expectedBrokerPort);
    assertThat(config.getCommunicatorMode()).isEqualTo(expectedCommunicatorMode);
    assertThat(config.getPositionX()).isEqualTo(expectedPositionX);
    assertThat(config.getPositionY()).isEqualTo(expectedPositionY);
    assertThat(config.getProcessingCapacity()).isEqualTo(expectedProcessingCapacity);
    assertThat(config.getAlgorithmMode()).isEqualTo(expectedAlgorithmMode);
    assertThat(config.getAlgorithmParam()).isEqualTo(expectedAlgorithmParam);
    assertThat(config.getTimeToOff()).isEqualTo(expectedTimeToOff);
    assertThat(config.getTimeToOn()).isEqualTo(expectedTimeToOn);
    assertThat(config.getTimeHysteresis()).isEqualTo(expectedTimeHysteresis);
    assertThat(config.call()).isZero();
  }
}
