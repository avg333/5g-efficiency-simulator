package userequipment;

import static java.util.Objects.nonNull;

import communication.ClientCommunicator;
import communication.ClientCommunicatorTCP;
import communication.ClientCommunicatorUDP;
import communication.CommunicatorMode;
import distribution.Distribution;
import distribution.DistributionMode;
import domain.Position;
import picocli.CommandLine;
import task.TaskGenerator;

public class UserEquipmentFactory {

  public UserEquipment createUserEquipment(final String[] args) {
    final UserEquipmentConfigDto config = createConfigDto(args);

    final Distribution mobilityDist =
        new Distribution(
            DistributionMode.fromCode(config.getMobilityDistributionMode()),
            config.getMobilityDistributionParam1(),
            config.getMobilityDistributionParam2());

    final Distribution delayDist =
        new Distribution(
            DistributionMode.fromCode(config.getDelayDistributionMode()),
            config.getDelayDistributionParam1(),
            config.getDelayDistributionParam2());

    final Distribution sizeDist =
        new Distribution(
            DistributionMode.fromCode(config.getSizeDistributionMode()),
            config.getSizeDistributionParam1(),
            config.getSizeDistributionParam2());

    if (nonNull(config.getSeed())) {
      sizeDist.setSeed(config.getSeed());
      delayDist.setSeed(config.getSeed() + 1L);
      mobilityDist.setSeed(config.getSeed() + 2L);
    }

    return new UserEquipment(
        createClientCommunicator(config),
        new Position(config.getPositionX(), config.getPositionY()),
        mobilityDist,
        new TaskGenerator(sizeDist, delayDist));
  }

  private UserEquipmentConfigDto createConfigDto(final String[] args) {
    final UserEquipmentConfigDto config = new UserEquipmentConfigDto();
    new CommandLine(config).execute(args);
    return config;
  }

  private ClientCommunicator createClientCommunicator(final UserEquipmentConfigDto config) {
    return CommunicatorMode.fromCode(config.getCommunicatorMode()) == CommunicatorMode.TCP
        ? new ClientCommunicatorTCP(config.getBrokerIp(), config.getBrokerPort())
        : new ClientCommunicatorUDP(config.getBrokerIp(), config.getBrokerPort());
  }
}
