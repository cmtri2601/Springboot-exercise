package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.common.audit.Auditable;
import nc.solon.common.constant.Action;
import nc.solon.common.constant.ErrorMessage;
import nc.solon.common.constant.Kafka;
import nc.solon.common.constant.LogMessage;
import nc.solon.common.dto.ManualConsumeTaxOutDTO;
import nc.solon.common.event.TaxCalculationEvent;
import nc.solon.person.config.KafkaProperties;
import nc.solon.person.entity.Person;
import nc.solon.person.repository.PersonRepository;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * The type Tax calculation consumer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationConsumer {
    private final PersonRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    /**
     * Consume.
     *
     * @param message the message
     * @param ack     the ack
     */
    @Auditable(action = Action.TAX_CONSUME)
    @KafkaListener(
            topics = "${spring.kafka.topics.tax-calculation.single.name}",
            groupId = "${spring.kafka.groups.tax-calculation.single.name}")
    public void consume(String message, Acknowledgment ack) {
        try {
            handleTaxCalculationEvent(message);
        } catch (Exception e) {
            handleSendRetryEvent(message, e);
        }
        ack.acknowledge();
    }

    /**
     * Retry consume.
     *
     * @param record the record
     * @param ack    the ack
     */
    @Auditable(action = Action.TAX_RETRY_CONSUME)
    @KafkaListener(
            topics = "${spring.kafka.topics.tax-calculation.retry.name}",
            groupId = "${spring.kafka.groups.tax-calculation.single.name}")
    public void retryConsume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String message = record.value();
        int retryCount = getRetryCount(record);

        try {
            handleTaxCalculationEvent(message);
        } catch (Exception e) {
            log.error(ErrorMessage.FAIL_PROCESS_KAFKA, message, e);

            String retryHeader = kafkaProperties.getTopics().getTaxCalculation().getRetry().getHeader();
            int maxRetries = kafkaProperties.getTopics().getTaxCalculation().getRetry().getMaxRetries();
            String retryTopic = kafkaProperties.getTopics().getTaxCalculation().getRetry().getName();
            String dltTopic = kafkaProperties.getTopics().getTaxCalculation().getDlt().getName();

            if (retryCount < maxRetries) {
                ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>(
                                retryTopic,
                                null,
                                null,
                                null,
                                message,
                                Collections.singletonList(
                                        new RecordHeader(
                                                retryHeader,
                                                String.valueOf(retryCount + 1).getBytes(StandardCharsets.UTF_8))));
                kafkaTemplate.send(producerRecord);
                log.info(LogMessage.SENT_TO_TOPIC, retryTopic);
                log.info(LogMessage.RETRY_COUNT, retryCount);
            } else {
                kafkaTemplate.send(dltTopic, message);
                log.info(LogMessage.SENT_TO_TOPIC, dltTopic);
            }
        }

        ack.acknowledge();
    }

    /**
     * Consume batch.
     *
     * @param batch the batch
     * @param ack   the ack
     */
    @Auditable(action = Action.TAX_BATCH_CONSUME)
    @KafkaListener(
            topics = "${spring.kafka.topics.tax-calculation.batch.name}",
            groupId = "${spring.kafka.groups.tax-calculation.batch.name}")
    public void consumeBatch(ConsumerRecord<String, String> batch, Acknowledgment ack) {
        log.info(LogMessage.RECEIVED_BATCH, batch);

        try {
            handleTaxCalculationEventBatch(batch.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error(ErrorMessage.FAIL_PROCESS_KAFKA_BATCH, batch.value(), e);
        }
    }

    /**
     * Consume manual manual consume tax out dto.
     *
     * @param count the count
     * @return the manual consume tax out dto
     */
    @Auditable(action = Action.TAX_MANUAL_CONSUME)
    public ManualConsumeTaxOutDTO consumeManual(int count) {
        List<TaxCalculationEvent> batch = new ArrayList<>();
        long totalLag = 0;
        int numberMessageLeft;
        boolean hasMessageLeft;

        Properties props = getManualConsumerProperties(count);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            String topic = kafkaProperties.getTopics().getTaxCalculation().getManual().getName();
            consumer.subscribe(Collections.singleton(topic));

            // Wait for assignment
            int retries = 0;
            ConsumerRecords<String, String> records = ConsumerRecords.empty();
            while (consumer.assignment().isEmpty() && retries++ < Kafka.MAX_RETRIES_ASSIGNMENT) {
                records = consumer.poll(Duration.ofMillis(Kafka.POLL_DURATION));
            }

            if (consumer.assignment().isEmpty()) {
                throw new IllegalStateException(ErrorMessage.FAIL_GET_PARTITION_ASSIGNMENT);
            }

            Set<TopicPartition> partitions = consumer.assignment();
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            Map<TopicPartition, OffsetAndMetadata> committedOffsets = consumer.committed(partitions);

            for (TopicPartition partition : partitions) {
                long end = endOffsets.getOrDefault(partition, 0L);
                long committed =
                        Optional.ofNullable(committedOffsets.get(partition))
                                .map(OffsetAndMetadata::offset)
                                .orElse(0L);
                totalLag += (end - committed);
            }

            if (records.count() == 0) {
                records = consumer.poll(Duration.ofMillis(Kafka.POLL_DURATION));
            }

            int totalRecords = records.count();
            numberMessageLeft = (int) (totalLag - totalRecords);
            hasMessageLeft = numberMessageLeft > 0;

            for (ConsumerRecord<String, String> record : records) {
                try {
                    TaxCalculationEvent event =
                            objectMapper.readValue(record.value(), TaxCalculationEvent.class);
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

    private Properties getManualConsumerProperties(int count) {
        KafkaProperties.Consumer consumerProps = kafkaProperties.getConsumer();
        KafkaProperties.Groups.TaxCalculation groups = kafkaProperties.getGroups().getTaxCalculation();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groups.getManual().getName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProps.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerProps.getValueDeserializer());
        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, groups.getManual().isEnableAutoCommitConfig());
        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, groups.getManual().getAutoOffsetResetConfig());
        props.put(
                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, groups.getManual().getSessionTimeoutMsConfig());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, count);
        return props;
    }

    private void handleTaxCalculationEvent(String message) throws JsonProcessingException {
        TaxCalculationEvent event = objectMapper.readValue(message, TaxCalculationEvent.class);
        updateTaxCalculationEvent(event);
    }

    private void handleTaxCalculationEventBatch(String message) throws JsonProcessingException {
        List<TaxCalculationEvent> events =
                objectMapper.readValue(message, new TypeReference<List<TaxCalculationEvent>>() {
                });
        for (TaxCalculationEvent event : events) {
            try {
                updateTaxCalculationEvent(event);
            } catch (Exception e) {
                handleSendRetryEvent(objectMapper.writeValueAsString(event), e);
            }
        }
    }

    private void updateTaxCalculationEvent(TaxCalculationEvent event) {
        Person existing =
                repository
                        .findByTaxId(event.getTaxId())
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                ErrorMessage.PERSON_NOT_FOUND_WITH_TAX_ID + event.getTaxId()));
        existing.setTaxDebt(existing.getTaxDebt().add(event.getAmount()));
        repository.save(existing);
    }

    private void handleSendRetryEvent(String message, Exception e) {
        log.error(ErrorMessage.FAIL_PROCESS_KAFKA, message, e);
        kafkaTemplate.send(
                kafkaProperties.getTopics().getTaxCalculation().getRetry().getName(), message);
        log.info(
                LogMessage.SENT_TO_TOPIC,
                kafkaProperties.getTopics().getTaxCalculation().getRetry().getName());
    }

    private int getRetryCount(ConsumerRecord<String, String> record) {
        String retryHeader = kafkaProperties.getTopics().getTaxCalculation().getRetry().getHeader();
        try {
            if (record.headers().lastHeader(retryHeader) != null) {
                return Integer.parseInt(new String(record.headers().lastHeader(retryHeader).value()));
            }
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }
}
