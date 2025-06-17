package nc.solon.person.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.event.PersonEvent;
import nc.solon.person.kafka.person.events.PersonEventsProducer;
import nc.solon.person.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceUnitTest {

  @Mock private PersonEventsProducer eventProducer;

  @Mock private PersonRepository personRepository;

  @InjectMocks private PersonService personService;

  private PersonInDTO personInDTO;
  private Person person;
  private PersonOutDTO personOutDTO;

  @BeforeEach
  void setUp() {
    // Create sample data for tests
    personInDTO =
        PersonInDTO.builder()
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .taxDebt(BigDecimal.valueOf(0))
            .build();

    person =
        Person.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .taxId("TX12345")
            .taxDebt(BigDecimal.valueOf(0))
            .build();

    personOutDTO =
        PersonOutDTO.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .age(35) // Age calculation is mocked implicitly
            .taxId("TX12345")
            .taxDebt(BigDecimal.valueOf(0))
            .build();
  }

  @Test
  void createPerson_shouldSendCreateEvent() {
    // When
    personService.createPerson(personInDTO);

    // Then
    ArgumentCaptor<PersonEvent> eventCaptor = ArgumentCaptor.forClass(PersonEvent.class);
    verify(eventProducer).sendEvent(eventCaptor.capture());

    PersonEvent capturedEvent = eventCaptor.getValue();
    assertEquals(PersonEvent.EventType.CREATE, capturedEvent.getEventType());
    assertNull(capturedEvent.getPersonId());
    assertEquals(personInDTO, capturedEvent.getPayload());
  }

  @Test
  void updatePerson_shouldSendUpdateEvent() {
    // Given
    Long personId = 1L;

    // When
    personService.updatePerson(personId, personInDTO);

    // Then
    ArgumentCaptor<PersonEvent> eventCaptor = ArgumentCaptor.forClass(PersonEvent.class);
    verify(eventProducer).sendEvent(eventCaptor.capture());

    PersonEvent capturedEvent = eventCaptor.getValue();
    assertEquals(PersonEvent.EventType.UPDATE, capturedEvent.getEventType());
    assertEquals(personId, capturedEvent.getPersonId());
    assertEquals(personInDTO, capturedEvent.getPayload());
  }

  @Test
  void deletePerson_shouldSendDeleteEvent() {
    // Given
    Long personId = 1L;

    // When
    personService.deletePerson(personId);

    // Then
    ArgumentCaptor<PersonEvent> eventCaptor = ArgumentCaptor.forClass(PersonEvent.class);
    verify(eventProducer).sendEvent(eventCaptor.capture());

    PersonEvent capturedEvent = eventCaptor.getValue();
    assertEquals(PersonEvent.EventType.DELETE, capturedEvent.getEventType());
    assertEquals(personId, capturedEvent.getPersonId());
    assertNull(capturedEvent.getPayload());
  }

  @Test
  void getPersonById_whenPersonExists_shouldReturnPerson() {
    // Given
    Long personId = 1L;
    when(personRepository.findById(personId)).thenReturn(Optional.of(person));

    // When
    Optional<PersonOutDTO> result = personService.getPersonById(personId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(personOutDTO.getId(), result.get().getId());
    assertEquals(personOutDTO.getFirstName(), result.get().getFirstName());
    assertEquals(personOutDTO.getLastName(), result.get().getLastName());
    assertEquals(personOutDTO.getTaxId(), result.get().getTaxId());

    verify(personRepository).findById(personId);
  }

  @Test
  void getPersonById_whenPersonDoesNotExist_shouldReturnEmpty() {
    // Given
    Long personId = 99L;
    when(personRepository.findById(personId)).thenReturn(Optional.empty());

    // When
    Optional<PersonOutDTO> result = personService.getPersonById(personId);

    // Then
    assertFalse(result.isPresent());
    verify(personRepository).findById(personId);
  }

  @Test
  void getPersonByTaxId_whenPersonExists_shouldReturnPerson() {
    // Given
    String taxId = "TX12345";
    when(personRepository.findByTaxId(taxId)).thenReturn(Optional.of(person));

    // When
    Optional<PersonOutDTO> result = personService.getPersonByTaxId(taxId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(personOutDTO.getId(), result.get().getId());
    assertEquals(personOutDTO.getFirstName(), result.get().getFirstName());
    assertEquals(personOutDTO.getLastName(), result.get().getLastName());
    assertEquals(personOutDTO.getTaxId(), result.get().getTaxId());

    verify(personRepository).findByTaxId(taxId);
  }

  @Test
  void getPersonByTaxId_whenPersonDoesNotExist_shouldReturnEmpty() {
    // Given
    String taxId = "NONEXISTENT";
    when(personRepository.findByTaxId(taxId)).thenReturn(Optional.empty());

    // When
    Optional<PersonOutDTO> result = personService.getPersonByTaxId(taxId);

    // Then
    assertFalse(result.isPresent());
    verify(personRepository).findByTaxId(taxId);
  }

  @Test
  void getAllPersons_whenPersonsExist_shouldReturnAllPersons() {
    // Given
    List<Person> persons = Collections.singletonList(person);
    when(personRepository.findAll()).thenReturn(persons);

    // When
    List<PersonOutDTO> result = personService.getAllPersons();

    // Then
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(personOutDTO.getId(), result.get(0).getId());
    assertEquals(personOutDTO.getFirstName(), result.get(0).getFirstName());
    assertEquals(personOutDTO.getLastName(), result.get(0).getLastName());
    assertEquals(personOutDTO.getTaxId(), result.get(0).getTaxId());

    verify(personRepository).findAll();
  }

  @Test
  void getAllPersons_whenNoPersonsExist_shouldReturnEmptyList() {
    // Given
    when(personRepository.findAll()).thenReturn(Collections.emptyList());

    // When
    List<PersonOutDTO> result = personService.getAllPersons();

    // Then
    assertTrue(result.isEmpty());
    verify(personRepository).findAll();
  }

  @Test
  void findPersonsByNamePrefixAndMinAge_shouldReturnFilteredPersons() {
    // Given
    String prefix = "Jo";
    int minAge = 30;
    LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);

    when(personRepository.findByNamePrefixAndOlderThan(prefix, maxBirthDate))
        .thenReturn(Collections.singletonList(person));

    // When
    List<PersonOutDTO> result = personService.findPersonsByNamePrefixAndMinAge(prefix, minAge);

    // Then
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(personOutDTO.getFirstName(), result.get(0).getFirstName());
    assertEquals(personOutDTO.getLastName(), result.get(0).getLastName());

    verify(personRepository).findByNamePrefixAndOlderThan(eq(prefix), any(LocalDate.class));
  }

  @Test
  void findPersonsByNamePrefixAndMinAge_whenNoMatchingPersons_shouldReturnEmptyList() {
    // Given
    String prefix = "Xyz";
    int minAge = 30;
    LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);

    when(personRepository.findByNamePrefixAndOlderThan(prefix, maxBirthDate))
        .thenReturn(Collections.emptyList());

    // When
    List<PersonOutDTO> result = personService.findPersonsByNamePrefixAndMinAge(prefix, minAge);

    // Then
    assertTrue(result.isEmpty());
    verify(personRepository).findByNamePrefixAndOlderThan(eq(prefix), any(LocalDate.class));
  }
}
