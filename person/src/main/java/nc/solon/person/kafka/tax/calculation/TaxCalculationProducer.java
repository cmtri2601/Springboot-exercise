package nc.solon.person.kafka.tax.calculation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nc.solon.person.constant.KafkaTopics;
import nc.solon.person.event.TaxCalculationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaxCalculationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEvent(TaxCalculationEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.TAX_CALCULATION, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
