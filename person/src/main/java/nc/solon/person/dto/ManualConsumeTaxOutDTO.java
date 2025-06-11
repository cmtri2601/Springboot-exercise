package nc.solon.person.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nc.solon.person.event.TaxCalculationEvent;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManualConsumeTaxOutDTO {
    public List<TaxCalculationEvent> batch;
    public int numberMessageLeft;
    public boolean hasMessageLeft;
}
