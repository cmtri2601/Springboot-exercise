package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.person.constant.KafkaTopics;
import nc.solon.person.entity.Person;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.repository.PersonRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationConsumer {
    private final PersonRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String RETRY_HEADER = "retry-count";
    private static final int MAX_RETRIES = 3;

    @KafkaListener(topics = KafkaTopics.TAX_CALCULATION, groupId = "person-group")
    public void consume(String message, Acknowledgment ack) {
        try {
            handleTaxCalculationEvent(message);
        } catch (Exception e) {
            handleSendRetryEvent(message, e);
        }
        ack.acknowledge();
    }

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
