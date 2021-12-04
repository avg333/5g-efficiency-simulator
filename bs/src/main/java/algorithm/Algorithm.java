package algorithm;

import basestation.BaseStation;
import basestation.Task;
import types.EventType;
import types.StateType;

import java.util.List;

public record Algorithm(BaseStation bs, AlgorithmMode mode, double c, double tToOff, double tToOn, double tHysteresis,
                        double algorithmParam) {

    public double processingAlgorithm() {
        double tProcess = -1;

        final boolean bsProcessing = this.bs.getCurrentTask() != null;
        final List<Task> tasksPending = this.bs.getTasksPending();
        final StateType bsState = this.bs.getStateX();

        if (!bsProcessing && !tasksPending.isEmpty() && bsState == StateType.ON) {
            final Task task = tasksPending.remove(0);
            this.bs.setCurrentTask(task);
            tProcess = task.getSize() / c;
        }

        return tProcess;
    }

    public double suspensionAlgorithm() {
        double tNewState = -1;

        final boolean bsProcessing = this.bs.getCurrentTask() != null;
        final List<Task> tasksPending = this.bs.getTasksPending();
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

    public double activationAlgorithm(final EventType eventType) {
        final List<Task> tasksPending = this.bs.getTasksPending();
        final StateType stateBs = bs.getStateX();


        if (!tasksPending.isEmpty() && bs.getStateX() == StateType.HYSTERESIS) {
            bs.setNextState(StateType.ON);
            return 0;
        }

        StateType nextState = null;

        switch (this.mode) {
            case NO_COALESCING -> nextState = activationNoCoalescing(tasksPending, stateBs);
            case SIZE_BASED_COALESCING -> nextState = activationSizeBasedCoalescing(tasksPending, stateBs);
            case TIME_BASED_COALESCING -> nextState = activationTimeBasedCoalescing(tasksPending, stateBs);
            case FIXED_COALESCING -> nextState = activationFixedCoalescing(tasksPending, stateBs, eventType);
        }

        if (nextState != null) {
            bs.setNextState(nextState);
            return 0;
        }

        return -1;
    }

    private StateType activationNoCoalescing(final List<Task> tasksPending, final StateType stateBs) {
        if (!tasksPending.isEmpty() && stateBs == StateType.OFF) {
            return StateType.TO_ON;
        }

        return null;
    }

    private StateType activationSizeBasedCoalescing(final List<Task> tasksPending, final StateType stateBs) {
        final double q = tasksPending.stream().mapToDouble(Task::getSize).sum();

        if (q > algorithmParam && stateBs == StateType.OFF) {
            return StateType.TO_ON;
        }

        return null;
    }

    private StateType activationTimeBasedCoalescing(final List<Task> tasksPending, final StateType stateBs) {
        if (!tasksPending.isEmpty() && stateBs == StateType.OFF) {
            return StateType.WAITING_TO_ON;
        }

        return null;
    }

    private StateType activationFixedCoalescing(final List<Task> tasksPending, final StateType stateBs, final EventType eventType) {
        if (stateBs == StateType.OFF && !tasksPending.isEmpty() && eventType == EventType.NEW_STATE) {
            return StateType.TO_ON;
        } else if (stateBs == StateType.OFF && eventType == EventType.NEW_STATE) {
            return StateType.OFF;
        }

        return null;
    }

    @Override
    public String toString() {
        return "mode=" + mode + ", algorithmParam=" + algorithmParam + ", c=" + c + ", tToOff=" + tToOff +
                ", tToOn=" + tToOn + ", tHysteresis=" + tHysteresis;
    }
}
