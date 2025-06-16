package nc.solon.person.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import nc.solon.person.constant.ErrorMessage;

/** The type Serialize. */
public class Serialize {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Audit string.
   *
   * @param obj the obj
   * @return the string
   */
  public static String audit(Object obj) {
    String result;
    try {
      result = objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      result = ErrorMessage.FAIL_SERIALIZE_ARGS + e.getMessage();
    }
    return result;
  }
}
