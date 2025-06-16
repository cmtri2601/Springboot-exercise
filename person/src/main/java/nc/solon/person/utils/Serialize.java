package nc.solon.person.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import nc.solon.person.constant.ErrorMessage;

public class Serialize {
  private static final ObjectMapper objectMapper = new ObjectMapper();

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
