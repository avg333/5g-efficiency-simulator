package broker;

import config.Config;
import routing.BsRouter;
import routing.RoutingAlgorithmMode;

public class BrokerFactory {
  private static final String PROP_FILE_NAME = "config.properties";

  public Broker createBroker() {
    final Config config = new Config(PROP_FILE_NAME);

    final int port = Integer.parseInt(config.getString("port"));
    final boolean communicatorModeTCP = Boolean.parseBoolean(config.getString("tcp"));
    final boolean eventsLog = Boolean.parseBoolean(config.getString("eventsLog"));
    final RoutingAlgorithmMode routingAlgorithmMode =
        RoutingAlgorithmMode.getRoutingAlgorithmModeTypeByCode(
            config.getString("routingAlgorithmMode").charAt(0));
    final BsRouter bsRouter = new BsRouter(routingAlgorithmMode);
    final double tFinal = Double.parseDouble(config.getString("tFinal"));

    return new Broker(port, communicatorModeTCP, eventsLog, bsRouter, tFinal);
  }
}
