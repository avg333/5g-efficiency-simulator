package types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Constants {
  NO_NEXT_STATE(-1),
  NO_TASK_TO_PROCESS(-1);

  private final int value;
}
