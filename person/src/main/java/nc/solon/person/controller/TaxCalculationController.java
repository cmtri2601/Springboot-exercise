package nc.solon.person.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.service.PersonService;
import nc.solon.person.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tax-calculation")
@RequiredArgsConstructor
public class TaxCalculationController {
    private final TaxService taxService;

    @PostMapping
    public ResponseEntity<Void> calculateTax(@Valid @RequestBody TaxInDTO taxInDTO) throws JsonProcessingException {
        taxService.calculateTax(taxInDTO);
        return ResponseEntity.accepted().build();
    }
}
