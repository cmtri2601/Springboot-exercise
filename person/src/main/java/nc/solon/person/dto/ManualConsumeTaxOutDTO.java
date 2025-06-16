package nc.solon.person.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nc.solon.person.event.TaxCalculationEvent;

/** The type Manual consume tax out dto. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManualConsumeTaxOutDTO {
  /** The Batch. */
  public List<TaxCalculationEvent> batch;

  /** The Number message left. */
  public int numberMessageLeft;

  /** The Has message left. */
  public boolean hasMessageLeft;
}
