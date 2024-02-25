package userequipment;

import communication.Communicator;
import communication.CommunicatorTCP;
import communication.CommunicatorUDP;
import config.Config;
import distribution.Distribution;
import distribution.DistributionMode;
import domain.Position;
import task.TaskGenerator;

public class UserEquipmentFactory {

  private static final String PROP_FILE_NAME = "config.properties";

  public UserEquipment createUserEquipment() {
    Config config = new Config(PROP_FILE_NAME);

    Position position = new Position(config.getDoubleProperty("x"), config.getDoubleProperty("y"));

    Distribution mobilityDist =
        new Distribution(
            DistributionMode.getDistributionModeByCode(
                config.getProperty("mobilityDistributionMode").charAt(0)),
            config.getDoubleProperty("mobilityDistributionParam1"),
            config.getDoubleProperty("mobilityDistributionParam2"));

    Distribution delayDist =
        new Distribution(
            DistributionMode.getDistributionModeByCode(
                config.getProperty("delayDistributionMode").charAt(0)),
            config.getDoubleProperty("delayDistributionParam1"),
            config.getDoubleProperty("delayDistributionParam2"));

    Distribution sizeDist =
        new Distribution(
            DistributionMode.getDistributionModeByCode(
                config.getProperty("sizeDistributionMode").charAt(0)),
            config.getDoubleProperty("sizeDistributionParam1"),
            config.getDoubleProperty("sizeDistributionParam2"));

    int seed = config.getIntProperty("seed");

    if (seed != 0) {
      mobilityDist.setSeed(seed);
      delayDist.setSeed(seed);
      sizeDist.setSeed(seed);
    }

    TaskGenerator taskGenerator = new TaskGenerator(sizeDist, delayDist);

    Communicator communicator =
        config.getBooleanProperty("tcp")
            ? new CommunicatorTCP(
                config.getProperty("ipBroker"), config.getIntProperty("portBroker"))
            : new CommunicatorUDP(
                config.getProperty("ipBroker"), config.getIntProperty("portBroker"));

    return new UserEquipment(position, communicator, mobilityDist, taskGenerator);
  }
}
