package loggers;

import static java.util.Objects.nonNull;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

public class ProgressBarLogger implements AutoCloseable {

  private static final String TASK_NAME = "Simulation progress";

  private final boolean progressBar;
  private final double tFinal;
  private ProgressBar pbb;

  public ProgressBarLogger(final boolean progressBar, final double finalT) {
    if (progressBar) {
      pbb = new ProgressBarBuilder().setTaskName(TASK_NAME).setInitialMax((long) finalT).build();
      pbb.getStart();
    }
    this.progressBar = nonNull(progressBar);
    this.tFinal = finalT;
  }

  public void upgradeProgress(final double t) {
    if (progressBar) {
      pbb.stepTo((long) t);
    }
  }

  @Override
  public void close() {
    if (progressBar) {
      pbb.stepTo((long) tFinal);
      pbb.close();
    }
  }
}
