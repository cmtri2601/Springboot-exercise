package nc.solon.person.kafka;

import lombok.RequiredArgsConstructor;
import nc.solon.person.property.KafkaProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** The type Kafka topic config. */
@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

  private final KafkaProperties kafkaProperties;

  /**
   * Person events topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic personEventsTopic() {
    String name = kafkaProperties.getTopics().getPersonEvents().getName();
    return TopicBuilder.name(name).build();
  }

  /**
   * Tax calculation topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationTopic() {
    String name = kafkaProperties.getTopics().getTaxCalculation().getSingle().getName();
    return TopicBuilder.name(name).build();
  }

  /**
   * Tax calculation retry topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationRetryTopic() {
    String name = kafkaProperties.getTopics().getTaxCalculation().getRetry().getName();
    return TopicBuilder.name(name).build();
  }

  /**
   * Tax calculation dl topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationDLTopic() {
    String name = kafkaProperties.getTopics().getTaxCalculation().getDlt().getName();
    return TopicBuilder.name(name).build();
  }

  /**
   * Tax calculation batch topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationBatchTopic() {
    String name = kafkaProperties.getTopics().getTaxCalculation().getBatch().getName();
    return TopicBuilder.name(name).build();
  }

  /**
   * Tax calculation manual consume topic new topic.
   *
   * @return the new topic
   */
  @Bean
  public NewTopic taxCalculationManualConsumeTopic() {
    var manual = kafkaProperties.getTopics().getTaxCalculation().getManual();
    return TopicBuilder.name(manual.getName()).partitions(manual.getPartitions()).build();
  }
}
