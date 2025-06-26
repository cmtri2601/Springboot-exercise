package nc.solon.camunda.kafka.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nc.solon.common.constant.ErrorMessage;
import nc.solon.common.constant.Kafka;
import nc.solon.common.event.CamundaLogEvent;
import nc.solon.common.utils.ErrorHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * The type Person events producer.
 */
@Service
@RequiredArgsConstructor
public class CamundaProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Send event.
     *
     * @param event the event
     */
    public void sendEvent(CamundaLogEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(Kafka.Topics.CAMUNDA_DLT, json);
        } catch (Exception e) {
            ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
        }
    }
}
