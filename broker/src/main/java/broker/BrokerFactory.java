package broker;

import config.Config;
import routing.BsRouter;
import routing.RoutingAlgorithmMode;

public class BrokerFactory {
  private static final String PROP_FILE_NAME = "config.properties";

  public Broker createBroker() {
    final Config config = new Config(PROP_FILE_NAME);

    final int port = Integer.parseInt(config.getProperty("port"));
    final boolean communicatorModeTCP = Boolean.parseBoolean(config.getProperty("tcp"));
    final boolean eventsLog = Boolean.parseBoolean(config.getProperty("eventsLog"));
    final RoutingAlgorithmMode routingAlgorithmMode =
        RoutingAlgorithmMode.getRoutingAlgorithmModeTypeByCode(
            config.getProperty("routingAlgorithmMode").charAt(0));
    final BsRouter bsRouter = new BsRouter(routingAlgorithmMode);
    final double tFinal = Double.parseDouble(config.getProperty("tFinal"));

    return new Broker(port, communicatorModeTCP, eventsLog, bsRouter, tFinal);
  }
}
