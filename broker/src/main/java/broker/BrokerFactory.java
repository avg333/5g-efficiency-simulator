package broker;

import communication.RegisterServer;
import communication.RegisterServerTCP;
import communication.RegisterServerUDP;
import config.Config;
import routing.BsRouter;
import routing.RoutingAlgorithmMode;

public class BrokerFactory {
  private static final String PROP_FILE_NAME = "config.properties";

  public Broker createBroker() {
    final Config config = new Config(PROP_FILE_NAME);

    final int port = Integer.parseInt(config.getString("port"));
    final boolean communicatorModeTCP = Boolean.parseBoolean(config.getString("tcp"));
    final RoutingAlgorithmMode routingAlgorithmMode =
        RoutingAlgorithmMode.getRoutingAlgorithmModeTypeByCode(
            config.getString("routingAlgorithmMode").charAt(0));
    final BsRouter bsRouter = new BsRouter(routingAlgorithmMode);
    final double tFinal = Double.parseDouble(config.getString("tFinal"));

    final RegisterServer server =
        (communicatorModeTCP) ? new RegisterServerTCP(port) : new RegisterServerUDP(port);

    final BrokerConfig brokerConfig =
        new BrokerConfig(true, Boolean.parseBoolean(config.getString("eventsLog")), true, tFinal);

    return new Broker(brokerConfig, server, bsRouter);
  }
}
