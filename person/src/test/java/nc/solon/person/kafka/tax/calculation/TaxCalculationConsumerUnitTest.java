package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.repository.PersonRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxCalculationConsumerUnitTest {

  @Mock private PersonRepository repository;

  @Mock private ObjectMapper objectMapper;

  @Mock private KafkaTemplate<String, String> kafkaTemplate;

  @Mock private Acknowledgment acknowledgment;

  @InjectMocks private TaxCalculationConsumer taxCalculationConsumer;

  private Person testPerson;
  private TaxCalculationEvent testEvent;
  private String testJson;

  @BeforeEach
  void setUp() throws JsonProcessingException {
    // Setup test data
    testPerson =
        Person.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .taxId("123456789")
            .taxDebt(new BigDecimal("1000.00"))
            .build();

    testEvent = new TaxCalculationEvent("123456789", new BigDecimal("500.00"));
    testJson = "{\"taxId\":\"123456789\",\"amount\":500.00}";

    // Configure mocks for JSON serialization/deserialization
    when(objectMapper.readValue(testJson, TaxCalculationEvent.class)).thenReturn(testEvent);
  }

  @Test
  void consume_shouldUpdatePersonTaxDebt_whenValidMessageReceived() throws JsonProcessingException {
    // Arrange
    when(repository.findByTaxId("123456789")).thenReturn(Optional.of(testPerson));

    // Act
    taxCalculationConsumer.consume(testJson, acknowledgment);

    // Assert
    ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
    verify(repository).save(personCaptor.capture());
    verify(acknowledgment).acknowledge();

    Person savedPerson = personCaptor.getValue();
    assertEquals(new BigDecimal("1500.00"), savedPerson.getTaxDebt());
  }

  @Test
  void consume_shouldSendToRetryTopic_whenExceptionOccurs() throws JsonProcessingException {
    // Arrange
    when(repository.findByTaxId(anyString())).thenThrow(new RuntimeException("Test exception"));

    // Act
    taxCalculationConsumer.consume(testJson, acknowledgment);

    // Assert
    verify(kafkaTemplate).send(eq("tax-retry-topic"), eq(testJson));
    verify(acknowledgment).acknowledge();
  }

  @Test
  void retryConsume_shouldUpdatePersonTaxDebt_whenValidMessageReceived()
      throws JsonProcessingException {
    // Arrange
    ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", testJson);
    when(repository.findByTaxId("123456789")).thenReturn(Optional.of(testPerson));

    // Act
    taxCalculationConsumer.retryConsume(record, acknowledgment);

    // Assert
    verify(repository).save(any(Person.class));
    verify(acknowledgment).acknowledge();
  }

  @Test
  void retryConsume_shouldRetryWithIncrementedCount_whenExceptionOccursAndRetryCountLessThanMax()
      throws JsonProcessingException {
    // Arrange
    ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", testJson);
    record.headers().add(new RecordHeader("retry-count", "1".getBytes()));
    when(repository.findByTaxId(anyString())).thenThrow(new RuntimeException("Test exception"));

    // Act
    taxCalculationConsumer.retryConsume(record, acknowledgment);

    // Assert
    ArgumentCaptor<org.apache.kafka.clients.producer.ProducerRecord> producerRecordCaptor =
        ArgumentCaptor.forClass(org.apache.kafka.clients.producer.ProducerRecord.class);
    verify(kafkaTemplate).send(producerRecordCaptor.capture());
    verify(acknowledgment).acknowledge();

    org.apache.kafka.clients.producer.ProducerRecord producerRecord =
        producerRecordCaptor.getValue();
    assertEquals("tax-retry-topic", producerRecord.topic());
    assertEquals(testJson, producerRecord.value());
  }

  @Test
  void retryConsume_shouldSendToDltTopic_whenExceptionOccursAndRetryCountReachesMax()
      throws JsonProcessingException {
    // Arrange
    ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", testJson);
    record.headers().add(new RecordHeader("retry-count", "3".getBytes()));
    when(repository.findByTaxId(anyString())).thenThrow(new RuntimeException("Test exception"));

    // Act
    taxCalculationConsumer.retryConsume(record, acknowledgment);

    // Assert
    verify(kafkaTemplate).send("tax-dlt-topic", testJson);
    verify(acknowledgment).acknowledge();
  }

  @Test
  void consumeBatch_shouldProcessAllEvents_whenValidBatchReceived() throws JsonProcessingException {
    // Arrange
    String batchJson = "[{\"taxId\":\"123456789\",\"amount\":500.00}]";
    List<TaxCalculationEvent> events = Collections.singletonList(testEvent);
    ConsumerRecord<String, String> batch = new ConsumerRecord<>("topic", 0, 0, "key", batchJson);

    when(objectMapper.readValue(
            eq(batchJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
        .thenReturn(events);
    when(repository.findByTaxId("123456789")).thenReturn(Optional.of(testPerson));

    // Act
    taxCalculationConsumer.consumeBatch(batch, acknowledgment);

    // Assert
    verify(repository).save(any(Person.class));
    verify(acknowledgment).acknowledge();
  }

  @Test
  void consumeBatch_shouldNotAcknowledge_whenExceptionOccurs() throws JsonProcessingException {
    // Arrange
    String batchJson = "[{\"taxId\":\"123456789\",\"amount\":500.00}]";
    ConsumerRecord<String, String> batch = new ConsumerRecord<>("topic", 0, 0, "key", batchJson);

    when(objectMapper.readValue(
            eq(batchJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
        .thenThrow(new JsonProcessingException("Test exception") {});

    // Act
    taxCalculationConsumer.consumeBatch(batch, acknowledgment);

    // Assert
    verify(acknowledgment, never()).acknowledge();
  }
}
