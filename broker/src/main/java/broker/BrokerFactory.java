package broker;

import java.io.InputStream;
import java.util.Properties;
import routing.BsRouter;
import routing.RoutingAlgorithmMode;

public class BrokerFactory {
  private static final String PROP_FILE_NAME = "config.properties";

  public Broker createBroker() {
    final Properties prop = new Properties();

    try (final InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
      prop.load(inputStream);
    } catch (Exception e) {
      throw new RuntimeException("Error loading the properties.", e);
    }

    final int port = Integer.parseInt(prop.getProperty("port"));
    final boolean communicatorModeTCP = Boolean.parseBoolean(prop.getProperty("tcp"));
    final boolean eventsLog = Boolean.parseBoolean(prop.getProperty("eventsLog"));
    final char routingAlgorithmModeChar = prop.getProperty("routingAlgorithmMode").charAt(0);
    final RoutingAlgorithmMode routingAlgorithmMode =
        RoutingAlgorithmMode.getRoutingAlgorithmModeTypeByCode(routingAlgorithmModeChar);
    final double tFinal = Double.parseDouble(prop.getProperty("tFinal"));
    final BsRouter bsRouter = new BsRouter(routingAlgorithmMode);

    return new Broker(port, communicatorModeTCP, eventsLog, bsRouter, tFinal);
  }
}
