package broker;

import java.util.concurrent.Callable;
import lombok.Getter;
import picocli.CommandLine.Option;

@Getter
public class BrokerConfigDto implements Callable<Integer> {

  @Option(
      names = {"-p", "--port"},
      description = "Port",
      defaultValue = "3001")
  private int port;

  @Option(
      names = {"-m", "--communicator-mode"},
      description = "Communicator Mode",
      defaultValue = "TCP")
  private String communicatorMode;

  @Option(
      names = {"-t", "--tFinal"},
      description = "Simulation Duration",
      required = true)
  private Integer tFinal;

  @Option(
      names = {"-r", "--routing-algorithm-mode"},
      description = "Routing Algorithm Mode",
      defaultValue = "v")
  private String routingAlgorithmMode;

  @Option(
      names = {"-e", "--events-log"},
      description = "Activates the writing of a csv file with all simulation events",
      defaultValue = "false")
  private boolean eventsLog;

  @Override
  public Integer call() {
    return 0;
  }
}
