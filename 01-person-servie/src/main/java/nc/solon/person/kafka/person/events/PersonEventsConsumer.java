package nc.solon.person.kafka.person.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.common.audit.Auditable;
import nc.solon.common.constant.Action;
import nc.solon.common.constant.ErrorMessage;
import nc.solon.common.constant.LogMessage;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.event.PersonEvent;
import nc.solon.person.entity.Person;
import nc.solon.person.repository.PersonRepository;
import nc.solon.person.utils.TaxIdGenerator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * The type Person events consumer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersonEventsConsumer {
    private final PersonRepository repository;
    private final ObjectMapper objectMapper;
    private final TaxIdGenerator taxIdGenerator;

    /**
     * Consume.
     *
     * @param message the message
     * @param ack     the ack
     */
    @Auditable(action = Action.PERSON_CONSUME)
    @KafkaListener(
            topics = "${spring.kafka.topics.person-events.name}",
            groupId = "${spring.kafka.groups.person-events.name}")
    public void consume(String message, Acknowledgment ack) {
        try {
            PersonEvent event = objectMapper.readValue(message, PersonEvent.class);
            if (event == null || event.getEventType() == null) {
                log.warn(ErrorMessage.MALFORMED_EVENT, message);
                return;
            }

            switch (event.getEventType()) {
                case CREATE -> handleCreate(event);
                case UPDATE -> handleUpdate(event);
                case DELETE -> handleDelete(event.getPersonId());
                default -> log.warn(ErrorMessage.NOT_EXIST_EVENT_TYPE, event.getEventType());
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error(ErrorMessage.FAIL_PROCESS_KAFKA, message, e);
        }
    }

    private void handleCreate(PersonEvent event) {
        Person person = mapToPerson(event.getPayload());
        person.setTaxId(taxIdGenerator.generateTaxId());
        person.setTaxDebt(BigDecimal.ZERO);
        repository.save(person);
        log.info(LogMessage.PERSON_CREATED, person.getTaxId());
    }

    private void handleUpdate(PersonEvent event) {
        Long id = event.getPersonId();
        Person existing =
                repository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.PERSON_NOT_FOUND + id));
        Person updated = mapToPerson(event.getPayload());
        updated.setId(id);
        updated.setTaxId(existing.getTaxId());
        repository.save(updated);
        log.info(LogMessage.PERSON_UPDATED, id);
    }

    private void handleDelete(Long id) {
        repository.deleteById(id);
        log.info(LogMessage.PERSON_DELETED, id);
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
