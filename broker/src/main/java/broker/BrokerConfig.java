package broker;

public record BrokerConfig(
    boolean printResume, boolean printCsv, boolean progressBar, double finalT) {}
