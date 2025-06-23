package nc.solon.common.constant;

/** The type Error message. */
public class ErrorMessage {
  /** The constant FAIL_SERIALIZE_EVENT. */
  public static final String FAIL_SERIALIZE_EVENT = "Failed to serialize event";

  /** The constant FAIL_SERIALIZE_ARGS. */
  public static final String FAIL_SERIALIZE_ARGS = "Failed to serialize arguments";

  /** The constant MALFORMED_EVENT. */
  public static final String MALFORMED_EVENT = "Received null or malformed event: {}";

  /** The constant NOT_EXIST_EVENT_TYPE. */
  public static final String NOT_EXIST_EVENT_TYPE = "Not existent event type: {}";

  /** The constant PERSON_NOT_FOUND. */
  public static final String PERSON_NOT_FOUND = "Person not found with id: ";

  /** The constant PERSON_NOT_FOUND_WITH_TAX_ID. */
  public static final String PERSON_NOT_FOUND_WITH_TAX_ID = "Person not found with tax id: ";

  /** The constant FAIL_PROCESS_KAFKA. */
  public static final String FAIL_PROCESS_KAFKA = "Error processing kafka message: {}";

  /** The constant FAIL_PROCESS_KAFKA_BATCH. */
  public static final String FAIL_PROCESS_KAFKA_BATCH = "Error processing kafka batch messages: {}";

  /** The constant FAIL_GET_PARTITION_ASSIGNMENT. */
  public static final String FAIL_GET_PARTITION_ASSIGNMENT = "Failed to get partition assignments";
}
