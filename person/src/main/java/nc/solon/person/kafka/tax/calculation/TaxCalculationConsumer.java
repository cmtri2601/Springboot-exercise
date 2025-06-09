package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.person.PersonEvent;
import nc.solon.person.constant.KafkaTopics;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.repository.PersonRepository;
import nc.solon.person.utils.TaxIdGenerator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationConsumer {
    private final PersonRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.TAX_CALCULATION_TOPIC, groupId = "person-group")
    public void consume(String message) throws JsonProcessingException {
        try {
            TaxCalculationEvent event = objectMapper.readValue(message, TaxCalculationEvent.class);
            String taxId = event.getTaxId();
            BigDecimal amount = event.getAmount();

            Person existing = repository.findByTaxId(taxId)
                    .orElseThrow(() -> new EntityNotFoundException("Person not found with tax id: " + amount));

            existing.setTaxDebt(existing.getTaxDebt().add(amount));
            repository.save(existing);
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", message, e);
        }
    }
}
