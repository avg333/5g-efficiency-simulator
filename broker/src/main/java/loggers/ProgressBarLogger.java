package loggers;

import static java.util.Objects.nonNull;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

public class ProgressBarLogger implements AutoCloseable {

  private static final String TASK_NAME = "Simulation progress";

  private final boolean progressBar;
  private final long tFinal;
  private final ProgressBar pbb;

  public ProgressBarLogger(final boolean progressBar, final double tFinal) {
    this.tFinal = (long) tFinal;
    this.pbb =
        progressBar
            ? new ProgressBarBuilder().setTaskName(TASK_NAME).setInitialMax(this.tFinal).build()
            : null;
    this.progressBar = nonNull(pbb);
    if (this.progressBar) {
      pbb.getStart();
    }
  }

  public void upgradeProgress(final double t) {
    if (progressBar) {
      pbb.stepTo((long) t);
    }
  }

  @Override
  public void close() {
    if (progressBar) {
      pbb.stepTo(tFinal);
      pbb.close();
    }
  }
}
