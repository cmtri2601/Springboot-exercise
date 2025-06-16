package nc.solon.person.kafka.person.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nc.solon.person.constant.ErrorMessage;
import nc.solon.person.event.PersonEvent;
import nc.solon.person.utils.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/** The type Person events producer. */
@Service
@RequiredArgsConstructor
public class PersonEventsProducer {

  private final ObjectMapper objectMapper;
  private final KafkaTemplate<String, String> kafkaTemplate;

  @Value("${kafka.topics.person.events.name}")
  private String personEventsTopic;

  /**
   * Send event.
   *
   * @param event the event
   */
  public void sendEvent(PersonEvent event) {
    try {
      String json = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(personEventsTopic, json);
    } catch (Exception e) {
      ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
    }
  }
}
