package nc.solon.person.kafka.person.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nc.solon.common.constant.ErrorMessage;
import nc.solon.common.event.PersonEvent;
import nc.solon.common.utils.ErrorHandler;
import nc.solon.person.config.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * The type Person events producer.
 */
@Service
@RequiredArgsConstructor
public class PersonEventsProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    /**
     * Send event.
     *
     * @param event the event
     */
    public void sendEvent(PersonEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(kafkaProperties.getTopics().getPersonEvents().getName(), json);
        } catch (Exception e) {
            ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
        }
    }
}
