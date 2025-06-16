package nc.solon.person.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.event.PersonEvent;
import nc.solon.person.kafka.person.events.PersonEventsProducer;
import nc.solon.person.repository.PersonRepository;
import nc.solon.person.utils.MapToDTO;
import org.springframework.stereotype.Service;

/** The type Person service. */
@Service
@RequiredArgsConstructor
public class PersonService {

  private final PersonEventsProducer eventProducer;
  private final PersonRepository personRepository;

  /**
   * Create person.
   *
   * @param dto the dto
   */
  public void createPerson(PersonInDTO dto) {
    PersonEvent event = new PersonEvent(PersonEvent.EventType.CREATE, null, dto);
    eventProducer.sendEvent(event);
  }

  /**
   * Update person.
   *
   * @param id the id
   * @param dto the dto
   */
  public void updatePerson(Long id, PersonInDTO dto) {
    PersonEvent event = new PersonEvent(PersonEvent.EventType.UPDATE, id, dto);
    eventProducer.sendEvent(event);
  }

  /**
   * Delete person.
   *
   * @param id the id
   */
  public void deletePerson(Long id) {
    PersonEvent event = new PersonEvent(PersonEvent.EventType.DELETE, id, null);
    eventProducer.sendEvent(event);
  }

  /**
   * Gets person by id.
   *
   * @param id the id
   * @return the person by id
   */
  public Optional<PersonOutDTO> getPersonById(Long id) {
    return personRepository.findById(id).map(MapToDTO::person);
  }

  /**
   * Gets person by tax id.
   *
   * @param taxId the tax id
   * @return the person by tax id
   */
  public Optional<PersonOutDTO> getPersonByTaxId(String taxId) {
    return personRepository.findByTaxId(taxId).map(MapToDTO::person);
  }

  /**
   * Gets all persons.
   *
   * @return the all persons
   */
  public List<PersonOutDTO> getAllPersons() {
    return personRepository.findAll().stream().map(MapToDTO::person).collect(Collectors.toList());
  }

  /**
   * Find persons by name prefix and min age list.
   *
   * @param prefix the prefix
   * @param minAge the min age
   * @return the list
   */
  public List<PersonOutDTO> findPersonsByNamePrefixAndMinAge(String prefix, int minAge) {
    LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
    return personRepository.findByNamePrefixAndOlderThan(prefix, maxBirthDate).stream()
        .map(MapToDTO::person)
        .collect(Collectors.toList());
  }
}
