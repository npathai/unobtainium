package org.unobtanium;

public class StepFailureException extends Exception {
  public StepFailureException(Throwable cause) {
    super(cause);
  }

  public StepFailureException(String message) {
    super(message);
  }
}
