package nc.solon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nc.solon.common.event.TaxCalculationEvent;

import java.util.List;

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
