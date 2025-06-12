package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.person.audit.Auditable;
import nc.solon.person.constant.KafkaTopics;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.repository.PersonRepository;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationConsumer {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final PersonRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String RETRY_HEADER = "retry-count";
    private static final int MAX_RETRIES = 3;

    @Auditable(action = "Tax Consumer")
    @KafkaListener(topics = KafkaTopics.TAX_CALCULATION, groupId = "person-group")
    public void consume(String message, Acknowledgment ack) {
        try {
            handleTaxCalculationEvent(message);
        } catch (Exception e) {
            handleSendRetryEvent(message, e);
        }
        ack.acknowledge();
    }

    @Auditable(action = "Tax Retry Consumer")
    @KafkaListener(topics = KafkaTopics.TAX_CALCULATION_RETRY, groupId = "person-group")
    public void retryConsume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String message = record.value();
        int retryCount = getRetryCount(record);

        try {
            handleTaxCalculationEvent(message);
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}",  e.getMessage(), e);

            if (retryCount < MAX_RETRIES) {
                // retry 3 times
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                        KafkaTopics.TAX_CALCULATION_RETRY,
                        null,
                        null,
                        null,
                        message,
                        Collections.singletonList(
                                new RecordHeader(RETRY_HEADER, String.valueOf(retryCount + 1).getBytes(StandardCharsets.UTF_8))
                        )
                );
                kafkaTemplate.send(producerRecord);
                log.info("Sent message to retry topic, retry count: {}", retryCount);
            } else {
                kafkaTemplate.send(KafkaTopics.TAX_CALCULATION_DLT, message);
                log.info("Sent message to dead letter topic");
            }
        }
        ack.acknowledge();
    }

    @Auditable(action = "Tax Consumer Batch")
    @KafkaListener(topics = KafkaTopics.TAX_CALCULATION_BATCH, groupId = "person-group")
    public void consumeBatch(ConsumerRecord<String, String> batch, Acknowledgment ack) {
        log.info("Received batch: {}", batch);

        try {
            handleTaxCalculationEventBatch(batch.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing batch: {}", batch.value(), e);
        }
    }

    @Auditable(action = "Tax Consumer Manual")
    public ManualConsumeTaxOutDTO consumeManual(int count) {
        List<TaxCalculationEvent> batch = new ArrayList<>();
        long totalLag = 0;
        int numberMessageLeft;
        boolean hasMessageLeft;

        Properties props = getProperties(count);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singleton(KafkaTopics.TAX_CALCULATION_MANUAL_CONSUME));

            // Wait for assignment
            // 1. Trigger partition assignment
            int retries = 0, maxRetries = 100;
            ConsumerRecords<String, String> records = new ConsumerRecords<>(Collections.emptyMap());
            while (consumer.assignment().isEmpty() && retries++ < maxRetries) {
                records = consumer.poll(Duration.ofMillis(100));
            }
            if (consumer.assignment().isEmpty()) {
                throw new IllegalStateException("Failed to get partition assignments.");
            }

            Set<TopicPartition> partitions = consumer.assignment();
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            Map<TopicPartition, OffsetAndMetadata> committedOffsets = consumer.committed(partitions);

            for (TopicPartition partition : partitions) {
                long logEnd = endOffsets.getOrDefault(partition, 0L);
                OffsetAndMetadata committedMeta = committedOffsets.get(partition);
                long committed = committedMeta != null ? committedMeta.offset() : 0L;
                long lag = logEnd - committed;
                totalLag += lag;
            }

            // Avoid double poll
            if (records.count() == 0) {
                records = consumer.poll(Duration.ofMillis(500));
            }

            int totalRecords = records.count();
            numberMessageLeft = (int) (totalLag - totalRecords);
            hasMessageLeft = numberMessageLeft > 0;

            for (ConsumerRecord<String, String> record : records) {
                try {
                    TaxCalculationEvent event = objectMapper.readValue(record.value(), TaxCalculationEvent.class);
                    batch.add(event);
                    updateTaxCalculationEvent(event);
                } catch (Exception e) {
                    handleSendRetryEvent(record.value(), e);
                }
            }
            consumer.commitSync();
        }

        return new ManualConsumeTaxOutDTO(batch, numberMessageLeft, hasMessageLeft);
    }

    private Properties getProperties(int count) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "manual-tax-calc-consumer");

        // Deserialize as Strings
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Manual offset management
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // Fetch at most {count} records per poll
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, count);

        // Required: start from beginning if no committed offset exists
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // For debug
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000 * 5);

        return props;
    }

    private void handleTaxCalculationEvent(String message) throws JsonProcessingException {
        TaxCalculationEvent event = objectMapper.readValue(message, TaxCalculationEvent.class);
        updateTaxCalculationEvent(event);
    }

    private void handleTaxCalculationEventBatch(String message) throws JsonProcessingException {
        List<TaxCalculationEvent> events = objectMapper.readValue(message,
                new TypeReference<List<TaxCalculationEvent>>() {});

        events.forEach(event -> {
            try {
                updateTaxCalculationEvent(event);
            } catch (Exception e) {
                try {
                    String json = objectMapper.writeValueAsString(event);
                    handleSendRetryEvent(json, e);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
            }

        });
    }

    private void updateTaxCalculationEvent(TaxCalculationEvent event) {
        String taxId = event.getTaxId();
        BigDecimal amount = event.getAmount();

        Person existing = repository.findByTaxId(taxId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with tax id: " + taxId));

        existing.setTaxDebt(existing.getTaxDebt().add(amount));
        repository.save(existing);
    }

    private void handleSendRetryEvent(String message, Exception e)  {
        log.error("Error processing Kafka message: {}", e.getMessage(), e);
        kafkaTemplate.send(KafkaTopics.TAX_CALCULATION_RETRY, message);
        log.info("Sent message to tax.calculation retry topic");
    }

    private int getRetryCount(ConsumerRecord<String, String> record) {
        if (record.headers().lastHeader(RETRY_HEADER) != null) {
            String value = new String(record.headers().lastHeader(RETRY_HEADER).value());
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

}
