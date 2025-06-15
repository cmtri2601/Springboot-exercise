package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.person.audit.Auditable;
import nc.solon.person.constant.Action;
import nc.solon.person.constant.ErrorMessage;
import nc.solon.person.constant.LogMessage;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.repository.PersonRepository;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
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

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    @Value("${kafka.topics.tax.calculation.retry.name}")
    private String taxRetryTopic;
    @Value("${kafka.topics.tax.calculation.dlt.name}")
    private String taxDtlTopic;
    @Value("${kafka.topics.tax.calculation.manual.name}")
    private String taxManualTopic;
    @Value("${kafka.groups.tax.calculation.manual.name}")
    private String taxManualGroup;
    @Value("${kafka.groups.tax.calculation.manual.enable-auto-commit-config}")
    private String manualAutoCommit;
    @Value("${kafka.groups.tax.calculation.manual.auto-offset-reset-config}")
    private String manualAutoOffsetReset;
    @Value("${kafka.groups.tax.calculation.manual.session-timeout-ms-config}")
    private int manualSessionTimeoutMs;
    @Value("${kafka.topics.tax.calculation.retry.header}")
    private String retryHeader;
    @Value("${kafka.topics.tax.calculation.retry.max-retries}")
    private int retryMaxRetries;

    private final PersonRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Auditable(action = Action.TAX_CONSUME)
    @KafkaListener(topics = "${kafka.topics.tax.calculation.single.name}", groupId = "${kafka.groups.tax.calculation.single.name}")
    public void consume(String message, Acknowledgment ack) {
        try {
            handleTaxCalculationEvent(message);
        } catch (Exception e) {
            handleSendRetryEvent(message, e);
        }
        ack.acknowledge();
    }

    @Auditable(action = Action.TAX_RETRY_CONSUME)
    @KafkaListener(topics = "${kafka.topics.tax.calculation.retry.name}", groupId = "${kafka.groups.tax.calculation.single.name}")
    public void retryConsume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String message = record.value();
        int retryCount = getRetryCount(record);

        try {
            handleTaxCalculationEvent(message);
        } catch (Exception e) {
            log.error(ErrorMessage.FAIL_PROCESS_KAFKA, message, e);

            if (retryCount < retryMaxRetries) {
                // retry 3 times
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                        taxRetryTopic,
                        null,
                        null,
                        null,
                        message,
                        Collections.singletonList(
                                new RecordHeader(retryHeader, String.valueOf(retryCount + 1).getBytes(StandardCharsets.UTF_8))
                        )
                );
                kafkaTemplate.send(producerRecord);
                log.info(LogMessage.SENT_TO_TOPIC, taxRetryTopic);
                log.info(LogMessage.RETRY_COUNT, retryCount);
            } else {
                kafkaTemplate.send(taxDtlTopic, message);
                log.info(LogMessage.SENT_TO_TOPIC, taxDtlTopic);
            }
        }
        ack.acknowledge();
    }

    @Auditable(action = Action.TAX_BATCH_CONSUME)
    @KafkaListener(topics = "${kafka.topics.tax.calculation.batch.name}", groupId = "${kafka.groups.tax.calculation.batch.name}")
    public void consumeBatch(ConsumerRecord<String, String> batch, Acknowledgment ack) {
        log.info(LogMessage.RECEIVED_BATCH, batch);

        try {
            handleTaxCalculationEventBatch(batch.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error(ErrorMessage.FAIL_PROCESS_KAFKA_BATCH, batch.value(), e);
        }
    }

    @Auditable(action = Action.TAX_MANUAL_CONSUME)
    public ManualConsumeTaxOutDTO consumeManual(int count) {
        List<TaxCalculationEvent> batch = new ArrayList<>();
        long totalLag = 0;
        int numberMessageLeft;
        boolean hasMessageLeft;

        Properties props = getProperties(count);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singleton(taxManualTopic));

            // Wait for assignment
            int retries = 0, maxRetries = 100;
            ConsumerRecords<String, String> records = new ConsumerRecords<>(Collections.emptyMap());
            while (consumer.assignment().isEmpty() && retries++ < maxRetries) {
                records = consumer.poll(Duration.ofMillis(100));
            }

            if (consumer.assignment().isEmpty()) {
                throw new IllegalStateException(ErrorMessage.FAIL_GET_PARTITION_ASSIGNMENT);
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
        props.put(ConsumerConfig.GROUP_ID_CONFIG, taxManualGroup);

        // Deserialize as Strings
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);

        // Manual offset management
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, manualAutoCommit);

        // Fetch at most {count} records per poll
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, count);

        // Required: start from beginning if no committed offset exists
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, manualAutoOffsetReset);

        // For debug
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, manualSessionTimeoutMs);

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
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.PERSON_NOT_FOUND_WITH_TAX_ID + taxId));

        existing.setTaxDebt(existing.getTaxDebt().add(amount));
        repository.save(existing);
    }

    private void handleSendRetryEvent(String message, Exception e)  {
        log.error(ErrorMessage.FAIL_PROCESS_KAFKA, message, e);
        kafkaTemplate.send(taxRetryTopic, message);
        log.info(LogMessage.SENT_TO_TOPIC, taxRetryTopic);
    }

    private int getRetryCount(ConsumerRecord<String, String> record) {
        if (record.headers().lastHeader(retryHeader) != null) {
            String value = new String(record.headers().lastHeader(retryHeader).value());
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

}
