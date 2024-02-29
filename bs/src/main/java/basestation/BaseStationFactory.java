package basestation;

import algorithm.AlgorithmMode;
import communication.ClientCommunicator;
import communication.ClientCommunicatorTCP;
import communication.ClientCommunicatorUDP;
import config.Config;
import domain.Position;

public class BaseStationFactory {

  private static final String PROP_FILE_NAME = "config.properties";

  public BaseStation createBaseStation() {
    final Config config = new Config(PROP_FILE_NAME);

    Position position = new Position(config.getDouble("x"), config.getDouble("y"));

    ClientCommunicator communicator =
        config.getBoolean("tcp")
            ? new ClientCommunicatorTCP(config.getString("ipBroker"), config.getInt("portBroker"))
            : new ClientCommunicatorUDP(config.getString("ipBroker"), config.getInt("portBroker"));

    BaseStationConfig baseStationConfig =
        new BaseStationConfig(
            AlgorithmMode.getModeTypeByCode(config.getString("algorithmMode").charAt(0)),
            config.getDouble("c"),
            config.getDouble("tToOff"),
            config.getDouble("tToOn"),
            config.getDouble("tHysteresis"),
            config.getDouble("algorithmParam"));

    return new BaseStation(communicator, position, baseStationConfig);
  }
}
