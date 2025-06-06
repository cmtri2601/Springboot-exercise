package nc.solon.person.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.event.PersonEvent;
import nc.solon.person.repository.PersonRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonEventConsumer {

    private final PersonRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "person-events", groupId = "person-group")
    public void consume(String message) {
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
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", message, e);
        }
    }

    private void handleCreate(PersonEvent event) throws Exception {
        Person person = mapToPerson(event.getPayload());
        person.setTaxDebt(BigDecimal.ZERO); // ensure new person starts with 0 tax debt
        repository.save(person);
        log.info("Created person with taxId: {}", person.getTaxId());
    }

    private void handleUpdate(PersonEvent event) throws Exception {
        Long id = event.getPersonId();
        Person existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
        Person updated = mapToPerson(event.getPayload());
        updated.setId(id); // ensure we're updating the correct record
        repository.save(updated);
        log.info("Updated person with id: {}", id);
    }

    private void handleDelete(Long id) {
        repository.deleteById(id);
        log.info("Deleted person with id: {}", id);
    }

    private Person mapToPerson(String payload) throws Exception {
        PersonInDTO dto = objectMapper.readValue(payload, PersonInDTO.class);
        return Person.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .taxId(dto.getTaxId())
                .dateOfBirth(dto.getDateOfBirth())
                .taxDebt(dto.getTaxDebt())
                .build();
    }
}
