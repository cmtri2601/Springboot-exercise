package nc.solon.person.kafka.tax.calculation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import nc.solon.person.constant.ErrorMessage;
import nc.solon.person.event.TaxCalculationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/** The type Tax calculation producer unit test. */
@ExtendWith(MockitoExtension.class)
class TaxCalculationProducerUnitTest {

  private final String TOPIC_SINGLE = "tax-calculation-topic";
  private final String TOPIC_BATCH = "tax-calculation-batch-topic";
  private final String TOPIC_MANUAL = "tax-calculation-manual-topic";
  @Mock private KafkaTemplate<String, String> kafkaTemplate;
  @Mock private ObjectMapper objectMapper;
  @InjectMocks private TaxCalculationProducer taxCalculationProducer;
  @Captor private ArgumentCaptor<String> topicCaptor;
  @Captor private ArgumentCaptor<String> messageCaptor;
  @Captor private ArgumentCaptor<String> keyCaptor;
  private TaxCalculationEvent event;
  private String serializedEvent;

  /** Sets up. */
  @BeforeEach
  void setUp() {
    // Set up topic names in producer using reflection
    ReflectionTestUtils.setField(taxCalculationProducer, "taxCalculationTopic", TOPIC_SINGLE);
    ReflectionTestUtils.setField(taxCalculationProducer, "taxBatchTopic", TOPIC_BATCH);
    ReflectionTestUtils.setField(taxCalculationProducer, "taxManualConsumeTopic", TOPIC_MANUAL);

    // Create test event
    event = new TaxCalculationEvent("TAX-123", new BigDecimal("1000.00"));
    serializedEvent = "{\"taxId\":\"TAX-123\",\"amount\":1000.00}";
  }

  /**
   * Send event should serialize and send event.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  void sendEvent_shouldSerializeAndSendEvent() throws JsonProcessingException {
    // Given
    when(objectMapper.writeValueAsString(event)).thenReturn(serializedEvent);

    // When
    taxCalculationProducer.sendEvent(event);

    // Then
    verify(kafkaTemplate).send(topicCaptor.capture(), messageCaptor.capture());
    assertEquals(TOPIC_SINGLE, topicCaptor.getValue());
    assertEquals(serializedEvent, messageCaptor.getValue());
  }

  /**
   * Send event should throw runtime exception when serialization fails.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  void sendEvent_shouldThrowRuntimeException_whenSerializationFails()
      throws JsonProcessingException {
    // Given
    when(objectMapper.writeValueAsString(event))
        .thenThrow(new JsonProcessingException("Serialization error") {});

    // When/Then
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> taxCalculationProducer.sendEvent(event));
    assertTrue(exception.getMessage().contains(ErrorMessage.FAIL_SERIALIZE_EVENT));
  }

  /**
   * Send batch event should serialize and send batch.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  void sendBatchEvent_shouldSerializeAndSendBatch() throws JsonProcessingException {
    // Given
    List<TaxCalculationEvent> batch =
        Arrays.asList(
            new TaxCalculationEvent("TAX-123", new BigDecimal("1000.00")),
            new TaxCalculationEvent("TAX-456", new BigDecimal("2000.00")));
    String serializedBatch =
        "[{\"taxId\":\"TAX-123\",\"amount\":1000.00},{\"taxId\":\"TAX-456\",\"amount\":2000.00}]";

    when(objectMapper.writeValueAsString(batch)).thenReturn(serializedBatch);

    // When
    taxCalculationProducer.sendBatchEvent(batch);

    // Then
    verify(kafkaTemplate).send(topicCaptor.capture(), messageCaptor.capture());
    assertEquals(TOPIC_BATCH, topicCaptor.getValue());
    assertEquals(serializedBatch, messageCaptor.getValue());
  }

  /**
   * Send batch event should throw runtime exception when serialization fails.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  void sendBatchEvent_shouldThrowRuntimeException_whenSerializationFails()
      throws JsonProcessingException {
    // Given
    List<TaxCalculationEvent> batch = List.of(event);
    when(objectMapper.writeValueAsString(batch))
        .thenThrow(new JsonProcessingException("Serialization error") {});

    // When/Then
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> taxCalculationProducer.sendBatchEvent(batch));
    assertTrue(exception.getMessage().contains(ErrorMessage.FAIL_SERIALIZE_EVENT));
  }

  /**
   * Send manual event should serialize and send event with key.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  void sendManualEvent_shouldSerializeAndSendEventWithKey() throws JsonProcessingException {
    // Given
    when(objectMapper.writeValueAsString(event)).thenReturn(serializedEvent);

    // When
    taxCalculationProducer.sendManualEvent(event);

    // Then
    verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
    assertEquals(TOPIC_MANUAL, topicCaptor.getValue());
    assertEquals("TAX-123", keyCaptor.getValue());
    assertEquals(serializedEvent, messageCaptor.getValue());
  }

  /**
   * Send manual event should throw runtime exception when serialization fails.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  void sendManualEvent_shouldThrowRuntimeException_whenSerializationFails()
      throws JsonProcessingException {
    // Given
    when(objectMapper.writeValueAsString(event))
        .thenThrow(new JsonProcessingException("Serialization error") {});

    // When/Then
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> taxCalculationProducer.sendManualEvent(event));
    assertTrue(exception.getMessage().contains(ErrorMessage.FAIL_SERIALIZE_EVENT));
  }
}
