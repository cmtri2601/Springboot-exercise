package nc.solon.person.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** The type Tax calculation controller. */
@RestController
@RequestMapping("/api/v1/tax-calculation")
@RequiredArgsConstructor
public class TaxCalculationController {
  private final TaxService taxService;

  /**
   * Calculate tax response entity.
   *
   * @param dto the dto
   * @return the response entity
   */
  @PostMapping
  public ResponseEntity<Void> calculateTax(@Valid @RequestBody TaxInDTO dto) {
    taxService.calculateTax(dto);
    return ResponseEntity.accepted().build();
  }

  /**
   * Calculate tax batch response entity.
   *
   * @param batch the batch
   * @return the response entity
   */
  @PostMapping("/batch")
  public ResponseEntity<Void> calculateTaxBatch(@RequestBody List<@Valid TaxInDTO> batch) {
    taxService.calculateTaxBatch(batch);
    return ResponseEntity.accepted().build();
  }

  /**
   * Produce manual response entity.
   *
   * @param batch the batch
   * @return the response entity
   */
  @PostMapping("manual")
  public ResponseEntity<Void> produceManual(@RequestBody List<@Valid TaxInDTO> batch) {
    taxService.produceManual(batch);
    return ResponseEntity.accepted().build();
  }

  /**
   * Consume manual response entity.
   *
   * @param count the count
   * @return the response entity
   */
  @GetMapping("manual")
  public ResponseEntity<ManualConsumeTaxOutDTO> consumeManual(
      @RequestParam(value = "count", required = false, defaultValue = "10") String count) {
    return ResponseEntity.ok(taxService.consumeManual(Integer.parseInt(count)));
  }
}
