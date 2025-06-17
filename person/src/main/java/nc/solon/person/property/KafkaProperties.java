package nc.solon.person.property;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/** The type Kafka properties. */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

  @NotBlank private String bootstrapServers;

  @NotNull private Consumer consumer;

  @NotNull private Producer producer;

  @NotNull private Topics topics;

  @NotNull private Groups groups;

  /** The type Consumer. */
  // --- Consumer ---
  @Getter
  @Setter
  public static class Consumer {
    @NotBlank private String keyDeserializer;

    @NotBlank private String valueDeserializer;
  }

  /** The type Producer. */
  // --- Producer ---
  @Getter
  @Setter
  public static class Producer {
    @NotBlank private String keySerializer;

    @NotBlank private String valueSerializer;
  }

  /** The type Topics. */
  // --- Topics ---
  @Getter
  @Setter
  public static class Topics {
    @NotNull private PersonEvents personEvents;

    @NotNull private TaxCalculation taxCalculation;

    /** The type Person events. */
    @Getter
    @Setter
    public static class PersonEvents {
      @NotBlank private String name;

      @NotNull private FixedBackoff fixedBackoff;

      /** The type Fixed backoff. */
      @Getter
      @Setter
      public static class FixedBackoff {
        @Min(0)
        private int interval;

        @Min(1)
        private int maxAttempts;
      }
    }

    /** The type Tax calculation. */
    @Getter
    @Setter
    public static class TaxCalculation {
      @NotNull private Single single;
      @NotNull private Retry retry;
      @NotNull private Dlt dlt;
      @NotNull private Batch batch;
      @NotNull private Manual manual;

      /** The type Single. */
      @Getter
      @Setter
      public static class Single {
        @NotBlank private String name;
      }

      /** The type Retry. */
      @Getter
      @Setter
      public static class Retry {
        @NotBlank private String name;
        @NotBlank private String header;

        @Min(1)
        private int maxRetries;
      }

      /** The type Dlt. */
      @Getter
      @Setter
      public static class Dlt {
        @NotBlank private String name;
      }

      /** The type Batch. */
      @Getter
      @Setter
      public static class Batch {
        @NotBlank private String name;
      }

      /** The type Manual. */
      @Getter
      @Setter
      public static class Manual {
        @NotBlank private String name;

        @Min(1)
        private int partitions;
      }
    }
  }

  /** The type Groups. */
  // --- Groups ---
  @Getter
  @Setter
  public static class Groups {
    @NotNull private PersonEvents personEvents;

    @NotNull private TaxCalculation taxCalculation;

    /** The type Person events. */
    @Getter
    @Setter
    public static class PersonEvents {
      @NotBlank private String name;
    }

    /** The type Tax calculation. */
    @Getter
    @Setter
    public static class TaxCalculation {
      @NotNull private Single single;
      @NotNull private Batch batch;
      @NotNull private Manual manual;

      /** The type Single. */
      @Getter
      @Setter
      public static class Single {
        @NotBlank private String name;
      }

      /** The type Batch. */
      @Getter
      @Setter
      public static class Batch {
        @NotBlank private String name;
      }

      /** The type Manual. */
      @Getter
      @Setter
      public static class Manual {
        @NotBlank private String name;

        private boolean enableAutoCommitConfig;

        @NotBlank private String autoOffsetResetConfig;

        @Min(1000)
        private int sessionTimeoutMsConfig;
      }
    }
  }
}
