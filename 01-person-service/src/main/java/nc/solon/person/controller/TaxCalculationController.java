package nc.solon.person.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.common.dto.ManualConsumeTaxOutDTO;
import nc.solon.common.dto.TaxBatchInDTO;
import nc.solon.common.dto.TaxInDTO;
import nc.solon.common.response.ApiResponse;
import nc.solon.person.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The type Tax calculation controller.
 */
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
    public ResponseEntity<ApiResponse<Void>> calculateTax(@Valid @RequestBody TaxInDTO dto) {
        taxService.calculateTax(dto);
        return ApiResponse.responseAccepted();
    }

    /**
     * Calculate tax batch response entity.
     *
     * @param batchDto the batch
     * @return the response entity
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> calculateTaxBatch(@RequestBody @Valid TaxBatchInDTO batchDto) {
        taxService.calculateTaxBatch(batchDto.getBatch());
        return ApiResponse.responseAccepted();
    }

    /**
     * Produce manual response entity.
     *
     * @param batchDto the batch
     * @return the response entity
     */
    @PostMapping("manual")
    public ResponseEntity<ApiResponse<Void>> produceManual(@RequestBody @Valid TaxBatchInDTO batchDto) {
        taxService.produceManual(batchDto.getBatch());
        return ApiResponse.responseAccepted();
    }

    /**
     * Consume manual response entity.
     *
     * @param count the count
     * @return the response entity
     */
    @GetMapping("manual")
    public ResponseEntity<ApiResponse<ManualConsumeTaxOutDTO>> consumeManual(
            @RequestParam(value = "count", required = false, defaultValue = "10") String count) {
        return ApiResponse.responseSuccess(taxService.consumeManual(Integer.parseInt(count)));
    }
}
