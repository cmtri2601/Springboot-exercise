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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationConsumer {
    private final PersonRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

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
    public void retryConsume(String message, Acknowledgment ack) {

        try {
            handleTaxCalculationEvent(message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}",  e.getMessage(), e);
            kafkaTemplate.send(KafkaTopics.TAX_CALCULATION_DLT, message);
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
}
