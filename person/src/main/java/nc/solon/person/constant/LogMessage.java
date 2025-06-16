package nc.solon.person.constant;

public class LogMessage {
  public static final String PERSON_CREATED = "Created person with taxId: {}";
  public static final String PERSON_UPDATED = "Updated person with id: {}";
  public static final String PERSON_DELETED = "Deleted person with id: {}";
  public static final String SENT_TO_TOPIC = "Sent message to {} topic";
  public static final String RETRY_COUNT = "Retry count {}";
  public static final String RECEIVED_BATCH = "Received batch: {}";
  public static final String AUDIT_CONTROLLER_INCOMING =
      "[AUDIT] Incoming call: {}.{}() with arguments: {}";
  public static final String AUDIT_CONTROLLER_COMPLETED =
      "[AUDIT] Completed call: {}.{}() with result: {}";
  public static final String AUDIT_CONTROLLER_EXCEPTION =
      "[AUDIT] Exception in: {}.{}() with error: {}";
  public static final String AUDIT_METHOD_START = "AUDIT START - Action: {}, Method: {}, Args: {}";
  public static final String AUDIT_METHOD_END = "AUDIT END - Action: {}, Method: {}, Result: {}";
  public static final String AUDIT_METHOD_ERROR = "AUDIT ERROR - Action: {}, Method: {}, Error: {}";
}
