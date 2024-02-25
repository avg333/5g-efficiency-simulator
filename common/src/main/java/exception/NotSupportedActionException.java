package exception;

import communication.model.base.Dto;

public class NotSupportedActionException extends RuntimeException {
  public NotSupportedActionException(Dto dto) {
    super("Action not supported: " + dto.getIdentifier());
  }
}
