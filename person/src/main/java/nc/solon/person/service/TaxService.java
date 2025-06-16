package nc.solon.person.service;

import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.kafka.tax.calculation.TaxCalculationConsumer;
import nc.solon.person.kafka.tax.calculation.TaxCalculationProducer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxCalculationProducer taxProducer;
    private final TaxCalculationConsumer taxConsumer;

    public void calculateTax(TaxInDTO dto) {
        TaxCalculationEvent event = new TaxCalculationEvent(dto.getTaxId(), dto.getAmount());
        taxProducer.sendEvent(event);
    }

    public void calculateTaxBatch(List<TaxInDTO> taxBatch) {
        // Create a list of ProducerRecords
        List<TaxCalculationEvent> eventBatch = taxBatch
                .stream()
                .map(tax -> new TaxCalculationEvent(tax.getTaxId(), tax.getAmount()))
                .collect(Collectors.toList());

        // Send all messages in a single batch operation
        taxProducer.sendBatchEvent(eventBatch);
    }

    public void produceManual(List<TaxInDTO> taxBatch) {
        taxBatch.forEach(
                tax -> {
                    TaxCalculationEvent event = new TaxCalculationEvent(tax.getTaxId(), tax.getAmount());
                    taxProducer.sendManualEvent(event);
                }
        );
    }

    public ManualConsumeTaxOutDTO consumeManual(int count) {
        return taxConsumer.consumeManual(count);
    }
}
