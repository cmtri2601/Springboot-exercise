package nc.solon.person.utils;

public class ErrorHandler {
  public static void throwRuntimeError(String message, Exception e) {
    throw new RuntimeException(message + e.getMessage(), e);
  }
}
