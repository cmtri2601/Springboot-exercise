package nc.solon.person.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The type Tax calculation event. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationEvent {
  private String taxId;
  private BigDecimal amount;
}
