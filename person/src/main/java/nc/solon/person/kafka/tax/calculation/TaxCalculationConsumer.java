package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationConsumer {
    private final PersonRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String RETRY_HEADER = "retry-count";
    private static final int MAX_RETRIES = 2;

    @KafkaListener(topics = KafkaTopics.TAX_CALCULATION, groupId = "person-group")
    public void consume(String message, Acknowledgment ack) {

        try {
            handleTaxCalculationEvent(message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
            kafkaTemplate.send(KafkaTopics.TAX_CALCULATION_RETRY, message);
        }
    }

    @KafkaListener(topics = KafkaTopics.TAX_CALCULATION_RETRY, groupId = "person-group")
    public void retryConsume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String message = record.value();
        int retryCount = getRetryCount(record);

        try {
            handleTaxCalculationEvent(message);
            ack.acknowledge();
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
            } else {
                kafkaTemplate.send(KafkaTopics.TAX_CALCULATION_DLT, message);
            }

        }
    }

    private void handleTaxCalculationEvent(String message) throws JsonProcessingException {
        TaxCalculationEvent event = objectMapper.readValue(message, TaxCalculationEvent.class);

        String taxId = event.getTaxId();
        BigDecimal amount = event.getAmount();

        Person existing = repository.findByTaxId(taxId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with tax id: " + taxId));

        existing.setTaxDebt(existing.getTaxDebt().add(amount));
        repository.save(existing);
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
