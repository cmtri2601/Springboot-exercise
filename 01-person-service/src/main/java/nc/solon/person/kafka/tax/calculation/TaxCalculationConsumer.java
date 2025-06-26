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
import nc.solon.person.entity.Person;
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

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Consume.
     *
     * @param message the message
     * @param ack     the ack
     */
    @Auditable(action = Action.TAX_CONSUME)
    @KafkaListener(topics = Kafka.Topics.TAX_CALCULATION_SINGLE, groupId = Kafka.Groups.TAX_CALCULATION_SINGLE)
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
    @KafkaListener(topics = Kafka.Topics.TAX_CALCULATION_RETRY, groupId = Kafka.Groups.TAX_CALCULATION_SINGLE)
    public void retryConsume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String message = record.value();
        int retryCount = getRetryCount(record);

        try {
            handleTaxCalculationEvent(message);
        } catch (Exception e) {
            log.error(ErrorMessage.FAIL_PROCESS_KAFKA, message, e);

            if (retryCount < Kafka.Topics.Config.TAX_CALCULATION_MAX_RETRIES) {
                ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>(
                                Kafka.Topics.TAX_CALCULATION_RETRY,
                                null,
                                null,
                                null,
                                message,
                                Collections.singletonList(
                                        new RecordHeader(
                                                Kafka.Topics.Config.TAX_CALCULATION_RETRY_HEADER,
                                                String.valueOf(retryCount + 1).getBytes(StandardCharsets.UTF_8))));
                kafkaTemplate.send(producerRecord);
                log.info(LogMessage.SENT_TO_TOPIC, Kafka.Topics.TAX_CALCULATION_RETRY);
                log.info(LogMessage.RETRY_COUNT, retryCount);
            } else {
                kafkaTemplate.send(Kafka.Topics.TAX_CALCULATION_DLT, message);
                log.info(LogMessage.SENT_TO_TOPIC, Kafka.Topics.TAX_CALCULATION_DLT);
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
    @KafkaListener(topics = Kafka.Topics.TAX_CALCULATION_BATCH, groupId = Kafka.Groups.TAX_CALCULATION_BATCH)
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
            consumer.subscribe(Collections.singleton(Kafka.Topics.TAX_CALCULATION_MANUAL));

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

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.Groups.TAX_CALCULATION_MANUAL);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Kafka.Consumer.KEY_DESERIALIZER);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Kafka.Consumer.VALUE_DESERIALIZER);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Kafka.Groups.Config.TAX_MANUAL_ENABLE_AUTO_COMMIT);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Kafka.Groups.Config.TAX_MANUAL_AUTO_OFFSET_RESET);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, Kafka.Groups.Config.TAX_MANUAL_SESSION_TIMEOUT_MS);
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
        kafkaTemplate.send(Kafka.Topics.TAX_CALCULATION_RETRY, message);
        log.info(LogMessage.SENT_TO_TOPIC, Kafka.Topics.TAX_CALCULATION_RETRY);
    }

    private int getRetryCount(ConsumerRecord<String, String> record) {
        try {
            if (record.headers().lastHeader(Kafka.Topics.Config.TAX_CALCULATION_RETRY_HEADER) != null) {
                return Integer.parseInt(new String(record.headers().lastHeader(Kafka.Topics.Config.TAX_CALCULATION_RETRY_HEADER).value()));
            }
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }
}
