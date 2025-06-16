package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nc.solon.person.constant.ErrorMessage;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.utils.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxCalculationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.tax.calculation.single.name}")
    private String taxCalculationTopic;

    @Value("${kafka.topics.tax.calculation.batch.name}")
    private String taxBatchTopic;

    @Value("${kafka.topics.tax.calculation.manual.name}")
    private String taxManualConsumeTopic;

    public void sendEvent(TaxCalculationEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(taxCalculationTopic, json);
        } catch (Exception e) {
            ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
        }
    }

    public void sendBatchEvent(List<TaxCalculationEvent> batch) {
        try {
            String json = objectMapper.writeValueAsString(batch);
            kafkaTemplate.send(taxBatchTopic, json);
        } catch (Exception e) {
            ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
        }
    }

    public void sendManualEvent(TaxCalculationEvent event) {
        try {
            String key = event.getTaxId();
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(taxManualConsumeTopic, key, json);
        } catch (Exception e) {
            ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
        }
    }
}
