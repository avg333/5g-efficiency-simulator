package broker;

import static org.assertj.core.api.Assertions.assertThat;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class BrokerConfigDtoTest {

  @Test
  void shouldParseCommandLineArguments() {
    int expectedPort = Instancio.create(Integer.class);
    String expectedCommunicatorMode = Instancio.create(String.class);
    int expectedTFinal = Instancio.create(Integer.class);
    String expectedRoutingAlgorithmMode = Instancio.create(String.class);

    final String[] args = {
        "-p", String.valueOf(expectedPort),
        "-m", expectedCommunicatorMode,
        "-t", String.valueOf(expectedTFinal),
        "-r", expectedRoutingAlgorithmMode,
        "-e", //FIXME
    };
    final BrokerConfigDto config = new BrokerConfigDto();
    new CommandLine(config).parseArgs(args);

    assertThat(config).isNotNull();
    assertThat(config.getPort()).isEqualTo(expectedPort);
    assertThat(config.getCommunicatorMode()).isEqualTo(expectedCommunicatorMode);
    assertThat(config.getTFinal()).isEqualTo(expectedTFinal);
    assertThat(config.getRoutingAlgorithmMode()).isEqualTo(expectedRoutingAlgorithmMode);
    assertThat(config.isEventsLog()).isTrue();
    assertThat(config.call()).isZero();
  }
}