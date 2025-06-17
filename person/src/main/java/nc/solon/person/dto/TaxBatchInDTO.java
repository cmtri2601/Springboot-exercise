package nc.solon.person.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The type Tax batch in dto. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxBatchInDTO {
  @NotEmpty @Valid private List<TaxInDTO> batch;
}
