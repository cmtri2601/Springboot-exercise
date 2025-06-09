package nc.solon.person.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.kafka.tax.calculation.TaxCalculationProducer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxCalculationProducer taxProducer;

    public void calculateTax(TaxInDTO dto) {
        TaxCalculationEvent event = new TaxCalculationEvent(dto.getTaxId(), dto.getAmount());
        taxProducer.sendEvent(event);
    }
}
