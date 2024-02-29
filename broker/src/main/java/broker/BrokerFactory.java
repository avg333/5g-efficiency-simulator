package broker;

import communication.RegisterServer;
import communication.RegisterServerTCP;
import communication.RegisterServerUDP;
import config.Config;
import loggers.LoggerCustom;
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

    final RegisterServer server =
        (communicatorModeTCP) ? new RegisterServerTCP(port) : new RegisterServerUDP(port);

    final LoggerCustom loggerCustom = new LoggerCustom(eventsLog);

    return new Broker(server, bsRouter, tFinal, loggerCustom);
  }
}
