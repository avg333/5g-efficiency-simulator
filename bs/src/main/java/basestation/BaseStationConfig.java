package basestation;

import algorithm.AlgorithmMode;

public record BaseStationConfig(
    AlgorithmMode mode,
    double c,
    double tToOff,
    double tToOn,
    double tHysteresis,
    double algorithmParam) {}
