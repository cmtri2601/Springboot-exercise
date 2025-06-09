package nc.solon.person.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.PersonEvent;
import nc.solon.person.kafka.person.events.PersonEventsProducer;
import nc.solon.person.repository.PersonRepository;
import nc.solon.person.utils.MapToDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonEventsProducer eventProducer;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;

    public void createPerson(PersonInDTO dto) throws JsonProcessingException {
        PersonEvent event = new PersonEvent(PersonEvent.EventType.CREATE, null, objectMapper.writeValueAsString(dto));
        eventProducer.sendEvent(event);
    }

    public void updatePerson(Long id, PersonInDTO dto) throws JsonProcessingException {
        PersonEvent event = new PersonEvent(PersonEvent.EventType.UPDATE, id, objectMapper.writeValueAsString(dto));
        eventProducer.sendEvent(event);
    }

    public void deletePerson(Long id) {
        PersonEvent event = new PersonEvent(PersonEvent.EventType.DELETE, id, null);
        eventProducer.sendEvent(event);
    }

    public Optional<PersonOutDTO> getPersonById(Long id) {
        return personRepository.findById(id).map(person -> MapToDTO.person(person));
    }

    public Optional<PersonOutDTO> getPersonByTaxId(String taxId) {
        return personRepository.findByTaxId(taxId).map(person -> MapToDTO.person(person));
    }

    public List<PersonOutDTO> getAllPersons() {
        return personRepository.findAll().stream()
                .map(person -> MapToDTO.person(person))
                .collect(Collectors.toList());
    }

    public List<PersonOutDTO> findPersonsByNamePrefixAndMinAge(String prefix, int minAge) {
        LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
        return personRepository.findByNamePrefixAndOlderThan(prefix, maxBirthDate).stream()
                .map(person -> MapToDTO.person(person))
                .collect(Collectors.toList());
    }
}
