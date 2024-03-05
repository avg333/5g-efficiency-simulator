package userequipment;

import java.util.concurrent.Callable;
import lombok.Getter;
import picocli.CommandLine.Option;

@Getter
public class UserEquipmentConfigDto implements Callable<Integer> {

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
      names = {"-r", "--seed"},
      description = "Seed")
  private Long seed;

  @Option(
      names = {"-sd", "--size-distribution-mode"},
      description = "Size Distribution Mode",
      required = true)
  private String sizeDistributionMode;

  @Option(
      names = {"-sp1", "--size-distribution-param1"},
      description = "Size Distribution Parameter 1",
      required = true)
  private double sizeDistributionParam1;

  @Option(
      names = {"-sp2", "--size-distribution-param2"},
      description = "Size Distribution Parameter 2",
      defaultValue = "0")
  private double sizeDistributionParam2;

  @Option(
      names = {"-dd", "--delay-distribution-mode"},
      description = "Delay Distribution Mode",
      required = true)
  private String delayDistributionMode;

  @Option(
      names = {"-dp1", "--delay-distribution-param1"},
      description = "Delay Distribution Parameter 1",
      required = true)
  private double delayDistributionParam1;

  @Option(
      names = {"-dp2", "--delay-distribution-param2"},
      description = "Delay Distribution Parameter 2",
      defaultValue = "0")
  private double delayDistributionParam2;

  @Option(
      names = {"-md", "--mobility-distribution-mode"},
      description = "Mobility Distribution Mode",
      defaultValue = "d")
  private String mobilityDistributionMode;

  @Option(
      names = {"-mp1", "--mobility-distribution-param1"},
      description = "Mobility Distribution Parameter 1",
      defaultValue = "0")
  private double mobilityDistributionParam1;

  @Option(
      names = {"-mp2", "--mobility-distribution-param2"},
      description = "Mobility Distribution Parameter 2",
      defaultValue = "0")
  private double mobilityDistributionParam2;

  @Override
  public Integer call() {
    return 0;
  }
}
