package algorithm;

import basestation.BaseStation;
import basestation.Task;
import types.StateType;

import java.util.Map;
import java.util.TreeMap;

public record Algorithm(BaseStation bs, AlgorithmMode mode, double c, double tToOff, double tToOn, double tHysteresis,
                        double algorithmParam) {

    public double processingAlgorithm() {
        double tProcess = -1;

        final boolean bsProcessing = this.bs.getCurrentTask() != null;
        final TreeMap<Long, Task> tasksPending = this.bs.getTasksPending();
        final StateType bsState = this.bs.getStateX();

        if (!bsProcessing && !tasksPending.isEmpty() && bsState == StateType.ON) {
            final Task task = tasksPending.pollFirstEntry().getValue();
            this.bs.setCurrentTask(task);
            tProcess = task.getSize() / c;

            if (!tasksPending.isEmpty()) {
                double q = this.bs.getQ();
                final Task taskCurrent = this.bs.getCurrentTask();
                q -= taskCurrent.getSize();
                this.bs.setQ(q);
            } else {
                this.bs.setQ(0.0);
            }
        }

        return tProcess;
    }

    public double suspensionAlgorithm() {
        double tNewState = -1;

        final boolean bsProcessing = this.bs.getCurrentTask() != null;
        final TreeMap<Long, Task> tasksPending = this.bs.getTasksPending();
        final StateType bsState = this.bs.getStateX();

        if (bsState == StateType.ON && tasksPending.isEmpty() && !bsProcessing) {
            if (tHysteresis == 0) {
                this.bs.setNextState(StateType.TO_OFF);
            } else {
                this.bs.setNextState(StateType.HYSTERESIS);
            }
            tNewState = 0;

        }

        return tNewState;
    }

    public double activationAlgorithm(boolean newState) {
        return switch (this.mode) {
            case NO_COALESCING -> activationNoCoalescing();
            case SIZE_BASED_COALESCING -> activationSizeBasedCoalescing();
            case TIME_BASED_COALESCING -> activationTimeBasedCoalescing();
            case FIXED_COALESCING -> activationFixedCoalescing(newState);
        };
    }

    private double activationNoCoalescing() {
        double tNewState = -1;

        if (!bs.getTasksPending().isEmpty() && bs.getStateX() == StateType.OFF) {
            bs.setNextState(StateType.TO_ON);
            tNewState = 0;
        } else if (!bs.getTasksPending().isEmpty() && bs.getStateX() == StateType.HYSTERESIS) {
            bs.setNextState(StateType.ON);
            tNewState = 0;
        }


        return tNewState;
    }

    /**
     * Transiciona la BS al estado en activación si el tamaño de la cola es superior al marcado
     *
     * @return unidades de tiempo hasta la activacion
     */
    private double activationSizeBasedCoalescing() {
        double tNewState = 0;
        double q = 0;

        for (Map.Entry<Long, Task> entry : bs.getTasksPending().entrySet()) {
            final Task currentTask = entry.getValue();
            q += currentTask.getSize();
        }

        if (bs.getStateX() == StateType.OFF && q > algorithmParam) {
            if (tToOn == 0) {
                bs.setState(StateType.ON);
            } else {
                bs.setState(StateType.TO_ON);
                bs.setNextState(StateType.ON);
                tNewState = tToOn;
            }

        } else if (bs.getStateX() == StateType.HYSTERESIS && !bs.getTasksPending().isEmpty()) {
            bs.setState(StateType.ON);
        }

        return tNewState;
    }

    private double activationTimeBasedCoalescing() {
        double tNewState = 0;

        if (!bs.getTasksPending().isEmpty()) {
            if (bs.getStateX() == StateType.OFF) {
                if (tToOn == 0 && algorithmParam == 0)
                    bs.setState(StateType.ON);
                else if (tToOn != 0 && algorithmParam == 0) {
                    bs.setState(StateType.TO_ON);
                    bs.setNextState(StateType.ON);
                    tNewState = tToOn;
                } else if (tToOn == 0) {
                    bs.setState(StateType.WAITING_TO_ON);
                    bs.setNextState(StateType.ON);
                    tNewState = algorithmParam;
                } else {
                    bs.setState(StateType.WAITING_TO_ON);
                    bs.setNextState(StateType.TO_ON);
                    tNewState = algorithmParam;
                }
            } else if (bs.getStateX() == StateType.HYSTERESIS) {
                bs.setState(StateType.ON);
            }
        }

        return tNewState;
    }

    private double activationFixedCoalescing(boolean newState) {
        double tNewState = 0;

        if (bs.getStateX() == StateType.OFF) {
            if (!bs.getTasksPending().isEmpty() && newState) {
                if (tToOn == 0)
                    bs.setState(StateType.ON);
                else {
                    bs.setState(StateType.TO_ON);
                    bs.setNextState(StateType.ON);
                    tNewState = tToOn;
                }
            } else if (newState) {
                bs.setNextState(StateType.OFF);
                tNewState = algorithmParam;
            }

        } else if (bs.getStateX() == StateType.HYSTERESIS && !bs.getTasksPending().isEmpty()) {
            bs.setState(StateType.ON);
        }

        return tNewState;
    }

    @Override
    public String toString() {
        return "mode=" + mode + ", algorithmParam=" + algorithmParam + ", c=" + c + ", tToOff=" + tToOff +
                ", tToOn=" + tToOn + ", tHysteresis=" + tHysteresis;
    }
}
