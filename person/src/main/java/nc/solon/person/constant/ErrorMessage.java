package nc.solon.person.constant;

public class ErrorMessage {
  public static final String FAIL_SERIALIZE_EVENT = "Failed to serialize event";
  public static final String FAIL_SERIALIZE_ARGS = "Failed to serialize arguments";
  public static final String MALFORMED_EVENT = "Received null or malformed event: {}";
  public static final String NOT_EXIST_EVENT_TYPE = "Not existent event type: {}";
  public static final String PERSON_NOT_FOUND = "Person not found with id: ";
  public static final String PERSON_NOT_FOUND_WITH_TAX_ID = "Person not found with tax id: ";
  public static final String FAIL_PROCESS_KAFKA = "Error processing kafka message: {}";
  public static final String FAIL_PROCESS_KAFKA_BATCH = "Error processing kafka batch messages: {}";
  public static final String FAIL_GET_PARTITION_ASSIGNMENT = "Failed to get partition assignments";
}
