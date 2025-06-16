package nc.solon.person.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** The type Kafka topic config. */
@Configuration
public class KafkaTopicConfig {
  @Value("${kafka.topics.person.events.name}")
  private String personEvents;

  @Value("${kafka.topics.tax.calculation.single.name}")
  private String taxCalculation;

  @Value("${kafka.topics.tax.calculation.retry.name}")
  private String taxRetry;

  @Value("${kafka.topics.tax.calculation.dlt.name}")
  private String taxDlt;

  @Value("${kafka.topics.tax.calculation.batch.name}")
  private String taxBatch;

  @Value("${kafka.topics.tax.calculation.manual.name}")
  private String taxManualConsume;

  @Value("${kafka.topics.tax.calculation.manual.partitions}")
  private int taxManualConsumePartitions;

  /**
   * Person events topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic personEventsTopic() {
    return TopicBuilder.name(personEvents).build();
  }

  /**
   * Tax calculation topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationTopic() {
    return TopicBuilder.name(taxCalculation).build();
  }

  /**
   * Tax calculation retry topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationRetryTopic() {
    return TopicBuilder.name(taxRetry).build();
  }

  /**
   * Tax calculation dl topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationDLTopic() {
    return TopicBuilder.name(taxDlt).build();
  }

  /**
   * Tax calculation batch topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationBatchTopic() {
    return TopicBuilder.name(taxBatch).build();
  }

  /**
   * Tax calculation manual consume topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationManualConsumeTopic() {
    return TopicBuilder.name(taxManualConsume).partitions(taxManualConsumePartitions).build();
  }
}
