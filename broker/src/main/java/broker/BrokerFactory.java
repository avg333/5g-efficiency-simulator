package broker;

import communication.CommunicatorMode;
import communication.RegisterServer;
import picocli.CommandLine;
import routing.BsRouter;
import routing.RoutingAlgorithmMode;

public class BrokerFactory {

  public Broker createBroker(final String[] args) {
    final BrokerConfigDto config = createConfigDto(args);

    return new Broker(
        createBrokerConfig(config), createRegisterServer(config), createBsRouter(config));
  }

  private BrokerConfig createBrokerConfig(final BrokerConfigDto config) {
    return new BrokerConfig(true, config.isEventsLog(), true, config.getTFinal());
  }

  private BsRouter createBsRouter(BrokerConfigDto config) {
    return new BsRouter(RoutingAlgorithmMode.fromValue(config.getRoutingAlgorithmMode()));
  }

  private BrokerConfigDto createConfigDto(final String[] args) {
    final BrokerConfigDto config = new BrokerConfigDto();
    new CommandLine(config).execute(args);
    return config;
  }

  private RegisterServer createRegisterServer(final BrokerConfigDto config) {
    return CommunicatorMode.fromValue(config.getCommunicatorMode()) == CommunicatorMode.TCP
        ? new RegisterServer(config.getPort())
        : new RegisterServer(config.getPort());
  }
}
