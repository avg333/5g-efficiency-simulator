package basestation;

import java.util.concurrent.Callable;
import lombok.Getter;
import picocli.CommandLine.Option;

@Getter
public class BaseStationConfigDto implements Callable<Integer> {

  @Option(
      names = {"-s", "--broker-ip"},
      description = "Broker IP",
      defaultValue = "localhost")
  private String brokerIp;

  @Option(
      names = {"-p", "--broker-port"},
      description = "Broker Port",
      defaultValue = "3001")
  private int brokerPort;

  @Option(
      names = {"-m", "--communicator-mode"},
      description = "Communicator Mode",
      defaultValue = "TCP")
  private String communicatorMode;

  @Option(
      names = {"-x", "--position-x"},
      description = "Position X",
      defaultValue = "0")
  private double positionX;

  @Option(
      names = {"-y", "--position-y"},
      description = "Position Y",
      defaultValue = "0")
  private double positionY;

  @Option(
      names = {"-c", "--processing-capacity"},
      description = "Processing Capacity",
      defaultValue = "1")
  private int processingCapacity;

  @Option(
      names = {"-am", "--algorithm-mode"},
      description = "Energy Saving Algorithm Type",
      defaultValue = "n")
  private String algorithmMode;

  @Option(
      names = {"-ap", "--algorithm-param"},
      description = "Algorithm Parameter",
      defaultValue = "1")
  private int algorithmParam;

  @Option(
      names = {"-toff", "--time-to-off"},
      description = "Time it takes to turn off since you decide to turn off",
      defaultValue = "0")
  private int timeToOff;

  @Option(
      names = {"-ton", "--time-to-on"},
      description = "Time it takes to turn on since you decide to turn on",
      defaultValue = "0")
  private int timeToOn;

  @Option(
      names = {"-th", "--time-hysteresis"},
      description =
          "Time the BS waits without receiving tasks from when it decides to turn off until it begins to turn off",
      defaultValue = "0")
  private int timeHysteresis;

  @Override
  public Integer call() {
    return 0;
  }
}
