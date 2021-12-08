package algorithm;

import basestation.BaseStation;
import basestation.Task;
import types.BsStateType;
import types.EventType;

import java.util.List;

public record Algorithm(BaseStation bs, AlgorithmMode mode, double c, double tToOff, double tToOn, double tHysteresis,
                        double algorithmParam) {

    public double processingAlgorithm() {
        double tProcess = -1;

        final boolean bsProcessing = this.bs.getCurrentTask() != null;
        final List<Task> tasksPending = this.bs.getTasksPending();
        final BsStateType bsState = this.bs.getStateX();

        if (!bsProcessing && !tasksPending.isEmpty() && bsState == BsStateType.ON) {
            final Task task = tasksPending.remove(0);
            this.bs.setCurrentTask(task);
            tProcess = task.size() / c;
        }

        return tProcess;
    }

    public double suspensionAlgorithm() {
        double tNewState = -1;

        final boolean bsProcessing = this.bs.getCurrentTask() != null;
        final List<Task> tasksPending = this.bs.getTasksPending();
        final BsStateType bsState = this.bs.getStateX();

        if (!bsProcessing && tasksPending.isEmpty() && bsState == BsStateType.ON) {
            this.bs.setNextState(BsStateType.HYSTERESIS);
            tNewState = 0;
        }

        return tNewState;
    }

    public double activationAlgorithm(final EventType eventType) {
        final List<Task> tasksPending = this.bs.getTasksPending();
        final BsStateType bsState = bs.getStateX();

        if (!tasksPending.isEmpty() && bs.getStateX() == BsStateType.HYSTERESIS) {
            bs.setNextState(BsStateType.ON);
            return 0;
        } else if (bsState != BsStateType.OFF) {
            return -1;
        }

        BsStateType nextState = null;

        switch (this.mode) {
            case NO_COALESCING -> nextState = (!tasksPending.isEmpty()) ? BsStateType.TO_ON : null;
            case SIZE_BASED_COALESCING -> nextState = activationSizeBasedCoalescing(tasksPending);
            case TIME_BASED_COALESCING -> nextState = (!tasksPending.isEmpty()) ? BsStateType.WAITING_TO_ON : null;
            case FIXED_COALESCING -> nextState = activationFixedCoalescing(tasksPending, eventType);
        }

        if (nextState != null) {
            bs.setNextState(nextState);
            return 0;
        }

        return -1;
    }

    private BsStateType activationSizeBasedCoalescing(final List<Task> tasksPending) {
        final double q = tasksPending.stream().mapToDouble(Task::size).sum();
        return (q > algorithmParam) ? BsStateType.TO_ON : null;
    }

    private BsStateType activationFixedCoalescing(final List<Task> tasksPending, final EventType eventType) {
        if (eventType == EventType.NEW_STATE) {
            return (!tasksPending.isEmpty()) ? BsStateType.WAITING_TO_ON : BsStateType.OFF;
        }
        return null;
    }

    @Override
    public String toString() {
        return "mode=" + mode + ", algorithmParam=" + algorithmParam + ", c=" + c + ", tToOff=" + tToOff +
                ", tToOn=" + tToOn + ", tHysteresis=" + tHysteresis;
    }
}
