package userequipment;

import static org.assertj.core.api.Assertions.assertThat;

import communication.CommunicatorMode;
import distribution.DistributionMode;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class UserEquipmentConfigDtoTest {

  @Test
  void shouldParseCommandLineArguments() {
    String expectedBrokerIp = Instancio.create(String.class);
    int expectedBrokerPort = Instancio.create(Integer.class);
    String expectedCommunicatorMode = Instancio.create(CommunicatorMode.class).getValue();
    double expectedPositionX = Instancio.create(Double.class);
    double expectedPositionY = Instancio.create(Double.class);
    Long expectedSeed = Instancio.create(Long.class);
    String expectedMobilityDistributionMode = Instancio.create(DistributionMode.class).getValue();
    double expectedMobilityDistributionParam1 = Instancio.create(Double.class);
    double expectedMobilityDistributionParam2 = Instancio.create(Double.class);
    String expectedSizeDistributionMode = Instancio.create(DistributionMode.class).getValue();
    double expectedSizeDistributionParam1 = Instancio.create(Double.class);
    double expectedSizeDistributionParam2 = Instancio.create(Double.class);
    String expectedDelayDistributionMode = Instancio.create(DistributionMode.class).getValue();
    double expectedDelayDistributionParam1 = Instancio.create(Double.class);
    double expectedDelayDistributionParam2 = Instancio.create(Double.class);

    final String[] args = {
      "-s", expectedBrokerIp,
      "-p", String.valueOf(expectedBrokerPort),
      "-m", expectedCommunicatorMode,
      "-x", String.valueOf(expectedPositionX),
      "-y", String.valueOf(expectedPositionY),
      "-r", String.valueOf(expectedSeed),
      "-md", expectedMobilityDistributionMode,
      "-mp1", String.valueOf(expectedMobilityDistributionParam1),
      "-mp2", String.valueOf(expectedMobilityDistributionParam2),
      "-sd", expectedSizeDistributionMode,
      "-sp1", String.valueOf(expectedSizeDistributionParam1),
      "-sp2", String.valueOf(expectedSizeDistributionParam2),
      "-dd", expectedDelayDistributionMode,
      "-dp1", String.valueOf(expectedDelayDistributionParam1),
      "-dp2", String.valueOf(expectedDelayDistributionParam2)
    };
    final UserEquipmentConfigDto config = new UserEquipmentConfigDto();
    new CommandLine(config).parseArgs(args);

    assertThat(config).isNotNull();
    assertThat(config.getBrokerIp()).isEqualTo(expectedBrokerIp);
    assertThat(config.getBrokerPort()).isEqualTo(expectedBrokerPort);
    assertThat(config.getCommunicatorMode()).isEqualTo(expectedCommunicatorMode);
    assertThat(config.getPositionX()).isEqualTo(expectedPositionX);
    assertThat(config.getPositionY()).isEqualTo(expectedPositionY);
    assertThat(config.getSeed()).isEqualTo(expectedSeed);
    assertThat(config.getMobilityDistributionMode()).isEqualTo(expectedMobilityDistributionMode);
    assertThat(config.getMobilityDistributionParam1())
        .isEqualTo(expectedMobilityDistributionParam1);
    assertThat(config.getMobilityDistributionParam2())
        .isEqualTo(expectedMobilityDistributionParam2);
    assertThat(config.getSizeDistributionMode()).isEqualTo(expectedSizeDistributionMode);
    assertThat(config.getSizeDistributionParam1()).isEqualTo(expectedSizeDistributionParam1);
    assertThat(config.getSizeDistributionParam2()).isEqualTo(expectedSizeDistributionParam2);
    assertThat(config.getDelayDistributionMode()).isEqualTo(expectedDelayDistributionMode);
    assertThat(config.getDelayDistributionParam1()).isEqualTo(expectedDelayDistributionParam1);
    assertThat(config.getDelayDistributionParam2()).isEqualTo(expectedDelayDistributionParam2);
    assertThat(config.call()).isZero();
  }
}
