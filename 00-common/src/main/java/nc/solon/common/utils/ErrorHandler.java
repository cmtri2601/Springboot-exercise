package nc.solon.common.utils;

/** The type Error handler. */
public class ErrorHandler {
  /**
   * Throw runtime error.
   *
   * @param message the message
   * @param e the e
   */
  public static void throwRuntimeError(String message, Exception e) {
    throw new RuntimeException(message + e.getMessage(), e);
  }
}
