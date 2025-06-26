package nc.solon.person.kafka;

import nc.solon.common.constant.Kafka;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * The type Kafka topic config.
 */
@Configuration
public class KafkaTopicConfig {
    /**
     * Person events topic new topic.
     *
     * @return the new topic
     */
    @Bean
    public NewTopic personEventsTopic() {
        return TopicBuilder.name(Kafka.Topics.PERSON_EVENTS).build();
    }

    /**
     * Tax calculation topic new topic.
     *
     * @return the new topic
     */
    @Bean
    public NewTopic taxCalculationTopic() {
        return TopicBuilder.name(Kafka.Topics.TAX_CALCULATION_SINGLE).build();
    }

    /**
     * Tax calculation retry topic new topic.
     *
     * @return the new topic
     */
    @Bean
    public NewTopic taxCalculationRetryTopic() {
        return TopicBuilder.name(Kafka.Topics.TAX_CALCULATION_RETRY).build();
    }

    /**
     * Tax calculation dl topic new topic.
     *
     * @return the new topic
     */
    @Bean
    public NewTopic taxCalculationDLTopic() {
        return TopicBuilder.name(Kafka.Topics.TAX_CALCULATION_DLT).build();
    }

    /**
     * Tax calculation batch topic new topic.
     *
     * @return the new topic
     */
    @Bean
    public NewTopic taxCalculationBatchTopic() {
        return TopicBuilder.name(Kafka.Topics.TAX_CALCULATION_BATCH).build();
    }

    /**
     * Tax calculation manual consume topic new topic.
     *
     * @return the new topic
     */
    @Bean
    public NewTopic taxCalculationManualConsumeTopic() {
        return TopicBuilder
                .name(Kafka.Topics.TAX_CALCULATION_MANUAL)
                .partitions(Kafka.Topics.Config.TAX_CALCULATION_MANUAL_PARTITIONS)
                .build();
    }
}
