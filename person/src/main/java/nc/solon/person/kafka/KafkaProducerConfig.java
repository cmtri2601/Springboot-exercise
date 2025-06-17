package nc.solon.person.kafka;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nc.solon.person.config.KafkaProperties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/** The type Kafka producer config. */
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

  private final KafkaProperties kafkaProperties;

  /**
   * Producer factory.
   *
   * @return the producer factory
   */
  @Bean
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    config.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        kafkaProperties.getProducer().getKeySerializer());
    config.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        kafkaProperties.getProducer().getValueSerializer());
    return new DefaultKafkaProducerFactory<>(config);
  }

  /**
   * Kafka template kafka template.
   *
   * @return the kafka template
   */
  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
