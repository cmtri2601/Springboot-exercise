package nc.solon.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** The type Tax calculation event. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationEvent {
  private String taxId;
  private BigDecimal amount;
}
