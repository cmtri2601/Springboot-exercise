package nc.solon.person.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tax-calculation")
@RequiredArgsConstructor
public class TaxCalculationController {
    private final TaxService taxService;

    @PostMapping
    public ResponseEntity<Void> calculateTax(@Valid @RequestBody TaxInDTO dto) {
        taxService.calculateTax(dto);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> calculateTaxBatch(@RequestBody List<@Valid TaxInDTO> batch) {
        taxService.calculateTaxBatch(batch);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("produce-manual")
    public ResponseEntity<Void> produceManual(@RequestBody List<@Valid TaxInDTO> batch) {
        taxService.produceManual(batch);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("consume-manual")
    public ResponseEntity<ManualConsumeTaxOutDTO> consumeManual(@RequestParam(value = "count", required = false, defaultValue = "10") String count) {
         return ResponseEntity.ok(taxService.consumeManual(Integer.parseInt(count)));
    }
}