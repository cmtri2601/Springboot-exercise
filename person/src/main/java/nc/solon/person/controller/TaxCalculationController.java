package nc.solon.person.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    @RequestMapping("/batch")
    public ResponseEntity<Void> calculateTaxBatch(@RequestBody List<@Valid TaxInDTO> batch) {
        taxService.calculateTaxBatch(batch);
        return ResponseEntity.accepted().build();
    }

    @PostMapping
    @RequestMapping("manual")
    public ResponseEntity<Void> calculateTaxManual(@Valid @RequestBody TaxInDTO dto) {
        taxService.calculateTaxManual(dto);
        return ResponseEntity.accepted().build();
    }
}
