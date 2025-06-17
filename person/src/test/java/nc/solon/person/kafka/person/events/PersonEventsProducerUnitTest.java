package nc.solon.person.kafka.person.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nc.solon.person.constant.ErrorMessage;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.event.PersonEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonEventsProducerUnitTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private PersonEventsProducer personEventsProducer;

    private PersonEvent personEvent;
    private String personEventsTopic = "person-events";
    private String serializedEvent = "{\"eventType\":\"CREATE\",\"personId\":1,\"payload\":{\"firstName\":\"John\"}}";

    @BeforeEach
    void setUp() {
        // Set the Kafka topic name through reflection, similar to how @Value would set it
        ReflectionTestUtils.setField(personEventsProducer, "personEventsTopic", personEventsTopic);

        // Create test data
        PersonInDTO personInDTO = PersonInDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .taxDebt(BigDecimal.valueOf(100.00))
                .build();

        personEvent = new PersonEvent(
                PersonEvent.EventType.CREATE,
                1L,
                personInDTO
        );
    }

    @Test
    void sendEvent_Success() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(any(PersonEvent.class))).thenReturn(serializedEvent);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(mock(CompletableFuture.class));

        // Act
        personEventsProducer.sendEvent(personEvent);

        // Assert
        verify(objectMapper, times(1)).writeValueAsString(personEvent);
        
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), messageCaptor.capture());
        
        assertEquals(personEventsTopic, topicCaptor.getValue());
        assertEquals(serializedEvent, messageCaptor.getValue());
    }

    @Test
    void sendEvent_SerializationError() throws JsonProcessingException {
        // Arrange
        JsonProcessingException exception = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(any(PersonEvent.class))).thenThrow(exception);

        // Act & Assert
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> personEventsProducer.sendEvent(personEvent)
        );

        // Verify the exception message contains the expected error message
        assertTrue(thrown.getMessage().startsWith(ErrorMessage.FAIL_SERIALIZE_EVENT));
        
        // Verify that KafkaTemplate.send was never called due to the exception
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }
}
