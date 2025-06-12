package nc.solon.person.kafka.person.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.person.audit.Auditable;
import nc.solon.person.constant.KafkaTopics;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.event.PersonEvent;
import nc.solon.person.repository.PersonRepository;
import nc.solon.person.utils.TaxIdGenerator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonEventsConsumer {
    private final PersonRepository repository;
    private final ObjectMapper objectMapper;
    private final TaxIdGenerator taxIdGenerator;

    @Auditable(action = "Person Consumer")
    @KafkaListener(topics = KafkaTopics.PERSON_EVENTS, groupId = "person-group")
    public void consume(String message, Acknowledgment ack) {
        try {
            PersonEvent event = objectMapper.readValue(message, PersonEvent.class);
            if (event == null || event.getEventType() == null) {
                log.warn("Received null or malformed event: {}", message);
                return;
            }

            switch (event.getEventType()) {
                case CREATE -> handleCreate(event);
                case UPDATE -> handleUpdate(event);
                case DELETE -> handleDelete(event.getPersonId());
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", message, e);
        }
    }

    private void handleCreate(PersonEvent event) {
        Person person = mapToPerson(event.getPayload());
        person.setTaxId(taxIdGenerator.generateTaxId());
        person.setTaxDebt(BigDecimal.ZERO); // ensure new person starts with 0 tax debt
        repository.save(person);
        log.info("Created person with taxId: {}", person.getTaxId());
    }

    private void handleUpdate(PersonEvent event) {
        Long id = event.getPersonId();
        Person existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
        Person updated = mapToPerson(event.getPayload());
        updated.setId(id); // ensure we're updating the correct record
        updated.setTaxId(existing.getTaxId());
        repository.save(updated);
        log.info("Updated person with id: {}", id);
    }

    private void handleDelete(Long id) {
        repository.deleteById(id);
        log.info("Deleted person with id: {}", id);
    }

    private Person mapToPerson(PersonInDTO dto) {
        return Person.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .taxDebt(dto.getTaxDebt())
                .build();
    }
}
