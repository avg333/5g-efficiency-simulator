package algorithm;

import basestation.BaseStation;
import basestation.Task;
import types.StateType;

import java.util.Map;
import java.util.TreeMap;

public record Algorithm(BaseStation bs, AlgorithmMode mode, double algorithmParam) {

    public double processingAlgorithm() {
        double tProcess = -1;

        final boolean bsProcessing = this.bs.isProcessing();
        final TreeMap<Long, Task> tasksPending = this.bs.getTasksPending();
        final StateType bsState = this.bs.getStateX();

        if (!bsProcessing && !tasksPending.isEmpty() && bsState == StateType.ON) {
            final Task task = tasksPending.pollFirstEntry().getValue();
            this.bs.setCurrentTask(task);
            this.bs.setProcessing(true);
            tProcess = task.getSize() / this.bs.getC();

            if (!tasksPending.isEmpty()) {
                double q = this.bs.getQ();
                final Task taskCurrent = this.bs.getCurrentTask();
                q -= taskCurrent.getSize();
                this.bs.setQ(q);
            } else
                this.bs.setQ(0.0);
        }

        return tProcess;
    }

    public double suspensionAlgorithm() {
        double tNewState = 0;

        final boolean bsProcessing = this.bs.isProcessing();
        final TreeMap<Long, Task> tasksPending = this.bs.getTasksPending();
        final StateType bsState = this.bs.getStateX();

        if (bsState == StateType.ON && tasksPending.isEmpty() && !bsProcessing) {
            if (this.bs.gettToOff() == 0 && this.bs.gettHysteresis() == 0) {
                this.bs.setState(StateType.OFF);
                if (mode == AlgorithmMode.FIXED_COALESCING) {
                    this.bs.setNextState(StateType.OFF);
                    tNewState = algorithmParam;
                }
            } else if (this.bs.gettHysteresis() == 0) {
                this.bs.setState(StateType.TO_OFF);
                this.bs.setNextState(StateType.OFF);
                tNewState = this.bs.gettToOff();
            } else if (this.bs.gettToOff() == 0) {
                this.bs.setState(StateType.HYSTERESIS);
                this.bs.setNextState(StateType.OFF);
                tNewState = this.bs.gettHysteresis();
            } else {
                this.bs.setState(StateType.HYSTERESIS);
                this.bs.setNextState(StateType.HYSTERESIS);
                tNewState = this.bs.gettHysteresis();
            }
        }

        return tNewState;
    }

    public double activationAlgorithm(boolean newState) {
        return switch (this.mode) {
            case NO_COALESCING -> activationNoCoalescing();
            case SIZE_BASED_COALESCING -> activationSizeBasedCoalescing();
            case TIME_BASED_COALESCING -> activationTimeBasedCoalescing();
            case FIXED_COALESCING -> activationFixedCoalescing(newState);
            case UNADMITTED -> 0;
        };
    }

    private double activationNoCoalescing() {
        double tNewState = 0;

        if (!bs.getTasksPending().isEmpty()) {
            if (bs.getStateX() == StateType.ON && bs.gettToOn() != 0) {
                bs.setState(StateType.TO_ON);
                bs.setNextState(StateType.ON);
                tNewState = bs.gettToOn();
            } else if (bs.getStateX() == StateType.HYSTERESIS) {
                bs.setState(StateType.ON);
            }
        }

        return tNewState;
    }

    private double activationSizeBasedCoalescing() {
        double tNewState = 0;
        double q = 0;

        for (Map.Entry<Long, Task> entry : bs.getTasksPending().entrySet()) {
            final Task currentTask = entry.getValue();
            q += currentTask.getSize();
        }

        if (bs.getStateX() == StateType.OFF && q > algorithmParam) {
            if (bs.gettToOn() == 0) {
                bs.setState(StateType.ON);
            } else {
                bs.setState(StateType.TO_ON);
                bs.setNextState(StateType.ON);
                tNewState = bs.gettToOn();
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
                if (bs.gettToOn() == 0 && algorithmParam == 0)
                    bs.setState(StateType.ON);
                else if (bs.gettToOn() != 0 && algorithmParam == 0) {
                    bs.setState(StateType.TO_ON);
                    bs.setNextState(StateType.ON);
                    tNewState = bs.gettToOn();
                } else if (bs.gettToOn() == 0) {
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
                if (bs.gettToOn() == 0)
                    bs.setState(StateType.ON);
                else {
                    bs.setState(StateType.TO_ON);
                    bs.setNextState(StateType.ON);
                    tNewState = bs.gettToOn();
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
        return "mode=" + mode + ", algorithmParam=" + algorithmParam;
    }
}
