package nc.solon.camunda.kafka;

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
     * Camunda dead letter topic new topic.
     *
     * @return the new topic
     */
    @Bean
    public NewTopic camundaDLTopic() {
        return TopicBuilder.name(Kafka.Topics.CAMUNDA_DLT).build();
    }
}
