package nc.solon.common.event;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * The type Tax calculation event.
 */
@Data
@AllArgsConstructor
public class TaxCalculationEvent {
    private String taxId;
    private BigDecimal amount;
}
