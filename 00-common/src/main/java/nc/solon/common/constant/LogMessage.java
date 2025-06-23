package nc.solon.common.constant;

/** The type Log message. */
public class LogMessage {
  /** The constant PERSON_CREATED. */
  public static final String PERSON_CREATED = "Created person with taxId: {}";

  /** The constant PERSON_UPDATED. */
  public static final String PERSON_UPDATED = "Updated person with id: {}";

  /** The constant PERSON_DELETED. */
  public static final String PERSON_DELETED = "Deleted person with id: {}";

  /** The constant SENT_TO_TOPIC. */
  public static final String SENT_TO_TOPIC = "Sent message to {} topic";

  /** The constant RETRY_COUNT. */
  public static final String RETRY_COUNT = "Retry count {}";

  /** The constant RECEIVED_BATCH. */
  public static final String RECEIVED_BATCH = "Received batch: {}";

  /** The constant AUDIT_CONTROLLER_INCOMING. */
  public static final String AUDIT_CONTROLLER_INCOMING =
      "[AUDIT] Incoming call: {}.{}() with arguments: {}";

  /** The constant AUDIT_CONTROLLER_COMPLETED. */
  public static final String AUDIT_CONTROLLER_COMPLETED =
      "[AUDIT] Completed call: {}.{}() with result: {}";

  /** The constant AUDIT_CONTROLLER_EXCEPTION. */
  public static final String AUDIT_CONTROLLER_EXCEPTION =
      "[AUDIT] Exception in: {}.{}() with error: {}";

  /** The constant AUDIT_METHOD_START. */
  public static final String AUDIT_METHOD_START = "AUDIT START - Action: {}, Method: {}, Args: {}";

  /** The constant AUDIT_METHOD_END. */
  public static final String AUDIT_METHOD_END = "AUDIT END - Action: {}, Method: {}, Result: {}";

  /** The constant AUDIT_METHOD_ERROR. */
  public static final String AUDIT_METHOD_ERROR = "AUDIT ERROR - Action: {}, Method: {}, Error: {}";
}
