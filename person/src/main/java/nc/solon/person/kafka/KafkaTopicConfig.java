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
        return TopicBuilder.name(KafkaTopics.PERSON_EVENTS).build();
    }

    @Bean
    public NewTopic personEventsRetryTopic() {
        return TopicBuilder.name(KafkaTopics.PERSON_EVENTS_RETRY).build();
    }

    @Bean
    public NewTopic personEventsDLTopic() {
        return TopicBuilder.name(KafkaTopics.PERSON_EVENTS_DLT).build();
    }

    @Bean
    public NewTopic taxCalculationTopic() {
        return TopicBuilder.name(KafkaTopics.TAX_CALCULATION).build();
    }


    @Bean
    public NewTopic taxCalculationRetryTopic() {
        return TopicBuilder.name(KafkaTopics.TAX_CALCULATION_RETRY).build();
    }


    @Bean
    public NewTopic taxCalculationDLTopic() {
    return TopicBuilder.name(KafkaTopics.TAX_CALCULATION_DLT).build();
}

}
