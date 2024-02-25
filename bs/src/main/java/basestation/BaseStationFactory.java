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

    Position position = new Position(config.getDoubleProperty("x"), config.getDoubleProperty("y"));

    Communicator communicator =
        config.getBooleanProperty("tcp")
            ? new CommunicatorTCP(config.getProperty("ipBroker"), config.getIntProperty("portBroker"))
            : new CommunicatorUDP(config.getProperty("ipBroker"), config.getIntProperty("portBroker"));

    BaseStationConfig baseStationConfig =
        new BaseStationConfig(
            AlgorithmMode.getModeTypeByCode(config.getProperty("algorithmMode").charAt(0)),
            config.getDoubleProperty("c"),
            config.getDoubleProperty("tToOff"),
            config.getDoubleProperty("tToOn"),
            config.getDoubleProperty("tHysteresis"),
            config.getDoubleProperty("algorithmParam"));

    return new BaseStation(position, communicator, baseStationConfig);
  }
}
