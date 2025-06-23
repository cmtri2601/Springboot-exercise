package nc.solon.person.service;

import lombok.RequiredArgsConstructor;
import nc.solon.common.dto.ManualConsumeTaxOutDTO;
import nc.solon.common.dto.TaxInDTO;
import nc.solon.common.event.TaxCalculationEvent;
import nc.solon.person.kafka.tax.calculation.TaxCalculationConsumer;
import nc.solon.person.kafka.tax.calculation.TaxCalculationProducer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Tax service.
 */
@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxCalculationProducer taxProducer;
    private final TaxCalculationConsumer taxConsumer;

    /**
     * Calculate tax.
     *
     * @param dto the dto
     */
    public void calculateTax(TaxInDTO dto) {
        TaxCalculationEvent event = new TaxCalculationEvent(dto.getTaxId(), dto.getAmount());
        taxProducer.sendEvent(event);
    }

    /**
     * Calculate tax batch.
     *
     * @param taxBatch the tax batch
     */
    public void calculateTaxBatch(List<TaxInDTO> taxBatch) {
        // Create a list of ProducerRecords
        List<TaxCalculationEvent> eventBatch =
                taxBatch.stream()
                        .map(tax -> new TaxCalculationEvent(tax.getTaxId(), tax.getAmount()))
                        .collect(Collectors.toList());

        // Send all messages in a single batch operation
        taxProducer.sendBatchEvent(eventBatch);
    }

    /**
     * Produce manual.
     *
     * @param taxBatch the tax batch
     */
    public void produceManual(List<TaxInDTO> taxBatch) {
        taxBatch.forEach(
                tax -> {
                    TaxCalculationEvent event = new TaxCalculationEvent(tax.getTaxId(), tax.getAmount());
                    taxProducer.sendManualEvent(event);
                });
    }

    /**
     * Consume manual manual consume tax out dto.
     *
     * @param count the count
     * @return the manual consume tax out dto
     */
    public ManualConsumeTaxOutDTO consumeManual(int count) {
        return taxConsumer.consumeManual(count);
    }
}
