package nc.solon.person.kafka.person.events;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nc.solon.person.event.PersonEvent;
import nc.solon.person.constant.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonEventsProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendEvent(PersonEvent event) {
        try {
           String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.PERSON_EVENTS, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event" + e.getMessage(), e);
        }
    }
}
