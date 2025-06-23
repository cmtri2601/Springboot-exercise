package nc.solon.camunda.kafka;

import lombok.RequiredArgsConstructor;
import nc.solon.camunda.config.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/** The type Kafka consumer config. */
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
  private final KafkaProperties kafkaProperties;

  /**
   * Consumer factory.
   *
   * @return the consumer factory
   */
  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    props.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        kafkaProperties.getConsumer().getKeyDeserializer());
    props.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        kafkaProperties.getConsumer().getValueDeserializer());
    return new DefaultKafkaConsumerFactory<>(props);
  }

  /**
   * Kafka listener container factory concurrent kafka listener container factory.
   *
   * @return the concurrent kafka listener container factory
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<String, String>();
    factory.setConsumerFactory(consumerFactory());
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    var fixedBackoff = kafkaProperties.getTopics().getPersonEvents().getFixedBackoff();
    DefaultErrorHandler errorHandler =
        new DefaultErrorHandler(
            new FixedBackOff(fixedBackoff.getInterval(), fixedBackoff.getMaxAttempts()));
    factory.setCommonErrorHandler(errorHandler);

    return factory;
  }
}
