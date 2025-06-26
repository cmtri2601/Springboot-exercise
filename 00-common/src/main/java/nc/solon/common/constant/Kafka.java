package nc.solon.common.constant;

/** The type Kafka. */
public class Kafka {
  public static class Topics {
    public static final String PERSON_EVENTS = "person.events";
    public static final String TAX_CALCULATION_SINGLE = "tax.calculation";
    public static final String TAX_CALCULATION_RETRY = "tax.calculation-retry";
    public static final String TAX_CALCULATION_DLT = "tax.calculation-dlt";
    public static final String TAX_CALCULATION_BATCH = "tax.calculation-batch";
    public static final String TAX_CALCULATION_MANUAL = "tax.calculation-manual-consume";
    public static final String CAMUNDA_DLT = "camunda-dlt";

    public static class Config {
      // person.events topic
      public static final int PERSON_EVENTS_BACKOFF_INTERVAL_MS = 0;
      public static final int PERSON_EVENTS_BACKOFF_MAX_ATTEMPTS = 3;

      // tax.calculation topics
      public static final String TAX_CALCULATION_RETRY_HEADER = "retry-count";
      public static final int TAX_CALCULATION_MAX_RETRIES = 3;
      public static final int TAX_CALCULATION_MANUAL_PARTITIONS = 3;
    }
  }

  public static class Groups {
    public static final String PERSON_EVENTS = "person-group";
    public static final String TAX_CALCULATION_SINGLE = "tax-single";
    public static final String TAX_CALCULATION_BATCH = "tax-batch";
    public static final String TAX_CALCULATION_MANUAL = "tax-manual";

    public static class Config {
      // tax.calculation manual group config
      public static final boolean TAX_MANUAL_ENABLE_AUTO_COMMIT = false;
      public static final String TAX_MANUAL_AUTO_OFFSET_RESET = "earliest";
      public static final int TAX_MANUAL_SESSION_TIMEOUT_MS = 300_000;
    }
  }

  public static class Consumer {
    public static final String KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String VALUE_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
  }

  public static class Producer {
    public static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
  }

  /** The constant POLL_DURATION. */
  public static final int POLL_DURATION = 100;

  /** The constant MAX_RETRIES_ASSIGNMENT. */
  public static final int MAX_RETRIES_ASSIGNMENT = 100;
}
