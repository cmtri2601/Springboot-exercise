package nc.solon.person.kafka.tax.calculation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nc.solon.common.constant.ErrorMessage;
import nc.solon.common.constant.Kafka;
import nc.solon.common.event.TaxCalculationEvent;
import nc.solon.common.utils.ErrorHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The type Tax calculation producer.
 */
@Service
@RequiredArgsConstructor
public class TaxCalculationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send event.
     *
     * @param event the event
     */
    public void sendEvent(TaxCalculationEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(Kafka.Topics.TAX_CALCULATION_SINGLE, json);
        } catch (Exception e) {
            ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
        }
    }

    /**
     * Send batch event.
     *
     * @param batch the batch
     */
    public void sendBatchEvent(List<TaxCalculationEvent> batch) {
        try {
            String json = objectMapper.writeValueAsString(batch);
            kafkaTemplate.send(Kafka.Topics.TAX_CALCULATION_BATCH, json);
        } catch (Exception e) {
            ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
        }
    }

    /**
     * Send manual event.
     *
     * @param event the event
     */
    public void sendManualEvent(TaxCalculationEvent event) {
        try {
            String key = event.getTaxId();
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(Kafka.Topics.TAX_CALCULATION_MANUAL, key, json);
        } catch (Exception e) {
            ErrorHandler.throwRuntimeError(ErrorMessage.FAIL_SERIALIZE_EVENT, e);
        }
    }
}
