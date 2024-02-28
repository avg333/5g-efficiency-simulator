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

    Position position = new Position(config.getDouble("x"), config.getDouble("y"));

    Distribution mobilityDist =
        new Distribution(
            DistributionMode.getDistributionModeByCode(
                config.getString("mobilityDistributionMode").charAt(0)),
            config.getDouble("mobilityDistributionParam1"),
            config.getDouble("mobilityDistributionParam2"));

    Distribution delayDist =
        new Distribution(
            DistributionMode.getDistributionModeByCode(
                config.getString("delayDistributionMode").charAt(0)),
            config.getDouble("delayDistributionParam1"),
            config.getDouble("delayDistributionParam2"));

    Distribution sizeDist =
        new Distribution(
            DistributionMode.getDistributionModeByCode(
                config.getString("sizeDistributionMode").charAt(0)),
            config.getDouble("sizeDistributionParam1"),
            config.getDouble("sizeDistributionParam2"));

    int seed = config.getInt("seed");

    if (seed != 0) {
      sizeDist.setSeed(seed);
      delayDist.setSeed(seed + 1L);
      mobilityDist.setSeed(seed + 2L);
    }

    TaskGenerator taskGenerator = new TaskGenerator(sizeDist, delayDist);

    Communicator communicator =
        config.getBoolean("tcp")
            ? new CommunicatorTCP(config.getString("ipBroker"), config.getInt("portBroker"))
            : new CommunicatorUDP(config.getString("ipBroker"), config.getInt("portBroker"));

    return new UserEquipment(position, communicator, mobilityDist, taskGenerator);
  }
}
