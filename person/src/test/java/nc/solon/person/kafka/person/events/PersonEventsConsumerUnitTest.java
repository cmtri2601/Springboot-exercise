package nc.solon.person.kafka.person.events;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.event.PersonEvent;
import nc.solon.person.repository.PersonRepository;
import nc.solon.person.utils.TaxIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class PersonEventsConsumerUnitTest {

  @Mock
  private PersonRepository personRepository;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private TaxIdGenerator taxIdGenerator;
  
  @Mock
  private Acknowledgment acknowledgment;

  @InjectMocks
  private PersonEventsConsumer personEventsConsumer;

  private PersonEvent createEvent;
  private PersonEvent updateEvent;
  private PersonEvent deleteEvent;
  private PersonInDTO personInDTO;
  private Person existingPerson;
  private String serializedCreateEvent;
  private String serializedUpdateEvent;
  private String serializedDeleteEvent;
  private String generatedTaxId = "TAX12345";

  @BeforeEach
  void setUp() {
    // Create test data
    personInDTO = PersonInDTO.builder()
        .firstName("John")
        .lastName("Doe")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .taxDebt(BigDecimal.valueOf(100.00))
        .build();

    createEvent = new PersonEvent(PersonEvent.EventType.CREATE, null, personInDTO);
    updateEvent = new PersonEvent(PersonEvent.EventType.UPDATE, 1L, personInDTO);
    deleteEvent = new PersonEvent(PersonEvent.EventType.DELETE, 1L, null);

    existingPerson = Person.builder()
        .id(1L)
        .firstName("Old First Name")
        .lastName("Old Last Name")
        .dateOfBirth(LocalDate.of(1980, 1, 1))
        .taxId(generatedTaxId)
        .taxDebt(BigDecimal.valueOf(200.00))
        .build();

    serializedCreateEvent = "{\"eventType\":\"CREATE\",\"personId\":null,\"payload\":{...}}";
    serializedUpdateEvent = "{\"eventType\":\"UPDATE\",\"personId\":1,\"payload\":{...}}";
    serializedDeleteEvent = "{\"eventType\":\"DELETE\",\"personId\":1,\"payload\":null}";
  }

  @Test
  void consume_CreateEvent_Success() throws JsonProcessingException {
    // Arrange
    when(objectMapper.readValue(serializedCreateEvent, PersonEvent.class)).thenReturn(createEvent);
    when(taxIdGenerator.generateTaxId()).thenReturn(generatedTaxId);

    // Act
    personEventsConsumer.consume(serializedCreateEvent, acknowledgment);

    // Assert
    ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
    verify(personRepository).save(personCaptor.capture());
    verify(acknowledgment).acknowledge();

    Person capturedPerson = personCaptor.getValue();
    assertEquals(personInDTO.getFirstName(), capturedPerson.getFirstName());
    assertEquals(personInDTO.getLastName(), capturedPerson.getLastName());
    assertEquals(personInDTO.getDateOfBirth(), capturedPerson.getDateOfBirth());
    assertEquals(generatedTaxId, capturedPerson.getTaxId());
    assertEquals(BigDecimal.ZERO, capturedPerson.getTaxDebt());
  }

  @Test
  void consume_UpdateEvent_Success() throws JsonProcessingException {
    // Arrange
    when(objectMapper.readValue(serializedUpdateEvent, PersonEvent.class)).thenReturn(updateEvent);
    when(personRepository.findById(1L)).thenReturn(Optional.of(existingPerson));

    // Act
    personEventsConsumer.consume(serializedUpdateEvent, acknowledgment);

    // Assert
    ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
    verify(personRepository).save(personCaptor.capture());
    verify(acknowledgment).acknowledge();

    Person capturedPerson = personCaptor.getValue();
    assertEquals(1L, capturedPerson.getId());
    assertEquals(personInDTO.getFirstName(), capturedPerson.getFirstName());
    assertEquals(personInDTO.getLastName(), capturedPerson.getLastName());
    assertEquals(personInDTO.getDateOfBirth(), capturedPerson.getDateOfBirth());
    assertEquals(generatedTaxId, capturedPerson.getTaxId());
    assertEquals(personInDTO.getTaxDebt(), capturedPerson.getTaxDebt());
  }

  @Test
  void consume_DeleteEvent_Success() throws JsonProcessingException {
    // Arrange
    when(objectMapper.readValue(serializedDeleteEvent, PersonEvent.class)).thenReturn(deleteEvent);

    // Act
    personEventsConsumer.consume(serializedDeleteEvent, acknowledgment);

    // Assert
    verify(personRepository).deleteById(1L);
    verify(acknowledgment).acknowledge();
  }

  @Test
  void consume_UpdateEvent_PersonNotFound() throws JsonProcessingException {
    // Arrange
    when(objectMapper.readValue(serializedUpdateEvent, PersonEvent.class)).thenReturn(updateEvent);
    when(personRepository.findById(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(EntityNotFoundException.class, () -> 
        personEventsConsumer.consume(serializedUpdateEvent, acknowledgment));
    
    verify(acknowledgment).acknowledge();
    verify(personRepository, never()).save(any(Person.class));
  }

  @Test
  void consume_MalformedEvent() throws JsonProcessingException {
    // Arrange
    when(objectMapper.readValue(anyString(), eq(PersonEvent.class))).thenReturn(null);

    // Act
    personEventsConsumer.consume("malformed-event", acknowledgment);

    // Assert
    verify(personRepository, never()).save(any(Person.class));
    verify(personRepository, never()).deleteById(anyLong());
    verify(acknowledgment).acknowledge();
  }

  @Test
  void consume_NullEventType() throws JsonProcessingException {
    // Arrange
    PersonEvent nullEventType = new PersonEvent(null, 1L, personInDTO);
    when(objectMapper.readValue(anyString(), eq(PersonEvent.class))).thenReturn(nullEventType);

    // Act
    personEventsConsumer.consume("null-event-type", acknowledgment);

    // Assert
    verify(personRepository, never()).save(any(Person.class));
    verify(personRepository, never()).deleteById(anyLong());
    verify(acknowledgment).acknowledge();
  }

  @Test
  void consume_SerializationError() throws JsonProcessingException {
    // Arrange
    when(objectMapper.readValue(anyString(), eq(PersonEvent.class))).thenThrow(new JsonProcessingException("Error processing JSON") {});

    // Act
    personEventsConsumer.consume("invalid-json", acknowledgment);

    // Assert
    verify(personRepository, never()).save(any(Person.class));
    verify(personRepository, never()).deleteById(anyLong());
    verify(acknowledgment).acknowledge();
  }
}
