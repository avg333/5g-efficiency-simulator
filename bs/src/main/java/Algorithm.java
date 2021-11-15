import java.util.Map;

enum modeType {
    NO_COALESCING(1), SIZE_BASED_COALESCING(4), TIME_BASED_COALESCING(5), FIXED_COALESCING(-1), UNADMITTED(0);

    private final int value;

    modeType(final int value) {
        this.value = value;
    }

    public static modeType getModeTypeByCode(final char code) {
        for (modeType e : modeType.values()) {
            if (code == e.value) return e;
        }
        return UNADMITTED;
    }
}

public class Algorithm {

    private Algorithm() {
    }

    public static double processingAlgorithm(BaseStation bs) {
        double tProcess = -1;

        if (!bs.procesando && !bs.listaTasksPendientes.isEmpty() && bs.state == stateType.ON) {
            bs.currentTask = bs.listaTasksPendientes.pollFirstEntry().getValue();
            bs.procesando = true;
            tProcess = bs.currentTask.getSize() / bs.c;

            if (!bs.listaTasksPendientes.isEmpty())
                bs.q -= bs.currentTask.getSize();
            else
                bs.q = 0.0;
        }

        return tProcess;
    }

    public static double suspensionAlgorithm(BaseStation bs) {
        double tNewState = 0;

        if (bs.state == stateType.ON && bs.listaTasksPendientes.isEmpty() && !bs.procesando) {
            if (bs.tToOff == 0 && bs.tHysterisis == 0) {
                bs.state = stateType.OFF;
                if (bs.algorithm == modeType.FIXED_COALESCING) {
                    bs.nextState = stateType.OFF;
                    tNewState = bs.algorithmParam;
                }
            } else if (bs.tHysterisis == 0) {
                bs.state = stateType.TO_OFF;
                bs.nextState = stateType.OFF;
                tNewState = bs.tToOff;
            } else if (bs.tToOff == 0) {
                bs.state = stateType.HISTERISIS;
                bs.nextState = stateType.OFF;
                tNewState = bs.tHysterisis;
            } else {
                bs.state = stateType.HISTERISIS;
                bs.nextState = stateType.TO_OFF;
                tNewState = bs.tHysterisis;
            }
        }

        return tNewState;
    }

    public static double activationAlgorithm(BaseStation bs, boolean newState) {
        return switch (bs.algorithm) {
            case NO_COALESCING -> activationNoCoalescing(bs);
            case SIZE_BASED_COALESCING -> activationSizeBasedCoalescing(bs);
            case TIME_BASED_COALESCING -> activationTimeBasedCoalescing(bs);
            case FIXED_COALESCING -> activationFixedCoalescing(bs, newState);
            case UNADMITTED -> 0;
        };
    }

    private static double activationNoCoalescing(BaseStation bs) {
        double tNewState = 0;

        if (!bs.listaTasksPendientes.isEmpty()) {
            if (bs.state == stateType.ON && bs.tToOn != 0) {
                bs.state = stateType.TO_ON;
                bs.nextState = stateType.ON;
                tNewState = bs.tToOn;
            } else if (bs.state == stateType.HISTERISIS) {
                bs.state = stateType.ON;
            }
        }

        return tNewState;
    }

    private static double activationSizeBasedCoalescing(BaseStation bs) {
        double tNewState = 0;
        double q = 0;

        for (Map.Entry<Long, Task> entry : bs.listaTasksPendientes.entrySet()) {
            final Task currentTask = entry.getValue();
            q += currentTask.getSize();
        }

        if (bs.state == stateType.OFF && q > bs.algorithmParam) {
            if (bs.tToOn == 0) {
                bs.state = stateType.ON;
            } else {
                bs.state = stateType.TO_ON;
                bs.nextState = stateType.ON;
                tNewState = bs.tToOn;
            }

        } else if (bs.state == stateType.HISTERISIS && !bs.listaTasksPendientes.isEmpty()) {
            bs.state = stateType.ON;
        }

        return tNewState;
    }

    private static double activationTimeBasedCoalescing(BaseStation bs) {
        double tNewState = 0;

        if (!bs.listaTasksPendientes.isEmpty()) {
            if (bs.state == stateType.OFF) {
                if (bs.tToOn == 0 && bs.algorithmParam == 0)
                    bs.state = stateType.ON;
                else if (bs.tToOn != 0 && bs.algorithmParam == 0) {
                    bs.state = stateType.TO_ON;
                    bs.nextState = stateType.ON;
                    tNewState = bs.tToOn;
                } else if (bs.tToOn == 0) {
                    bs.state = stateType.WAITING_TO_ON;
                    bs.nextState = stateType.ON;
                    tNewState = bs.algorithmParam;
                } else {
                    bs.state = stateType.WAITING_TO_ON;
                    bs.nextState = stateType.TO_ON;
                    tNewState = bs.algorithmParam;
                }
            } else if (bs.state == stateType.HISTERISIS) {
                bs.state = stateType.ON;
            }
        }

        return tNewState;
    }

    private static double activationFixedCoalescing(BaseStation bs, boolean newState) {
        double tNewState = 0;

        if (bs.state == stateType.OFF) {
            if (!bs.listaTasksPendientes.isEmpty() && newState) {
                if (bs.tToOn == 0)
                    bs.state = stateType.ON;
                else {
                    bs.state = stateType.TO_ON;
                    bs.nextState = stateType.ON;
                    tNewState = bs.tToOn;
                }
            } else if (newState) {
                bs.nextState = stateType.OFF;
                tNewState = bs.algorithmParam;
            }

        } else if (bs.state == stateType.HISTERISIS && !bs.listaTasksPendientes.isEmpty()) {
            bs.state = stateType.ON;
        }

        return tNewState;
    }
}
