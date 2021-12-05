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

        if (bsState == BsStateType.ON && tasksPending.isEmpty() && !bsProcessing) {
            if (tHysteresis == 0) {
                this.bs.setNextState(BsStateType.TO_OFF);
            } else {
                this.bs.setNextState(BsStateType.HYSTERESIS);
            }
            tNewState = 0;

        }

        return tNewState;
    }

    public double activationAlgorithm(final EventType eventType) {
        final List<Task> tasksPending = this.bs.getTasksPending();
        final BsStateType stateBs = bs.getStateX();


        if (!tasksPending.isEmpty() && bs.getStateX() == BsStateType.HYSTERESIS) {
            bs.setNextState(BsStateType.ON);
            return 0;
        }

        BsStateType nextState = null;

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

    private BsStateType activationNoCoalescing(final List<Task> tasksPending, final BsStateType stateBs) {
        if (!tasksPending.isEmpty() && stateBs == BsStateType.OFF) {
            return BsStateType.TO_ON;
        }

        return null;
    }

    private BsStateType activationSizeBasedCoalescing(final List<Task> tasksPending, final BsStateType stateBs) {
        final double q = tasksPending.stream().mapToDouble(Task::size).sum();

        if (q > algorithmParam && stateBs == BsStateType.OFF) {
            return BsStateType.TO_ON;
        }

        return null;
    }

    private BsStateType activationTimeBasedCoalescing(final List<Task> tasksPending, final BsStateType stateBs) {
        if (!tasksPending.isEmpty() && stateBs == BsStateType.OFF) {
            return BsStateType.WAITING_TO_ON;
        }

        return null;
    }

    private BsStateType activationFixedCoalescing(final List<Task> tasksPending, final BsStateType stateBs, final EventType eventType) {
        if (stateBs == BsStateType.OFF && !tasksPending.isEmpty() && eventType == EventType.NEW_STATE) {
            return BsStateType.TO_ON;
        } else if (stateBs == BsStateType.OFF && eventType == EventType.NEW_STATE) {
            return BsStateType.OFF;
        }

        return null;
    }

    @Override
    public String toString() {
        return "mode=" + mode + ", algorithmParam=" + algorithmParam + ", c=" + c + ", tToOff=" + tToOff +
                ", tToOn=" + tToOn + ", tHysteresis=" + tHysteresis;
    }
}
