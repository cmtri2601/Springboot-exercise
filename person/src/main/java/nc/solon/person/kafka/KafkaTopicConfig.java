package nc.solon.person.kafka;

import nc.solon.person.constant.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic personEventsTopic() {
        return TopicBuilder.name(KafkaTopics.PERSON_EVENTS_TOPIC).build();
    }

    @Bean
    public NewTopic personTopic() {
        return TopicBuilder.name(KafkaTopics.TAX_CALCULATION_TOPIC).build();
    }

}
