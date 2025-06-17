package nc.solon.person.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.dto.TaxBatchInDTO;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** The type Tax calculation controller. */
@RestController
@RequestMapping("/tax-calculation")
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
   * @param batchDto the batch
   * @return the response entity
   */
  @PostMapping("/batch")
  public ResponseEntity<Void> calculateTaxBatch(@RequestBody @Valid TaxBatchInDTO batchDto) {
    taxService.calculateTaxBatch(batchDto.getBatch());
    return ResponseEntity.accepted().build();
  }

  /**
   * Produce manual response entity.
   *
   * @param batchDto the batch
   * @return the response entity
   */
  @PostMapping("manual")
  public ResponseEntity<Void> produceManual(@RequestBody @Valid TaxBatchInDTO batchDto) {
    taxService.produceManual(batchDto.getBatch());
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
