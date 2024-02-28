package basestation;

import algorithm.AlgorithmMode;
import communication.Communicator;
import communication.CommunicatorTCP;
import communication.CommunicatorUDP;
import config.Config;
import domain.Position;

public class BaseStationFactory {

  private static final String PROP_FILE_NAME = "config.properties";

  public BaseStation createBaseStation() {
    final Config config = new Config(PROP_FILE_NAME);

    Position position = new Position(config.getDouble("x"), config.getDouble("y"));

    Communicator communicator =
        config.getBoolean("tcp")
            ? new CommunicatorTCP(config.getString("ipBroker"), config.getInt("portBroker"))
            : new CommunicatorUDP(config.getString("ipBroker"), config.getInt("portBroker"));

    BaseStationConfig baseStationConfig =
        new BaseStationConfig(
            AlgorithmMode.getModeTypeByCode(config.getString("algorithmMode").charAt(0)),
            config.getDouble("c"),
            config.getDouble("tToOff"),
            config.getDouble("tToOn"),
            config.getDouble("tHysteresis"),
            config.getDouble("algorithmParam"));

    return new BaseStation(position, communicator, baseStationConfig);
  }
}
