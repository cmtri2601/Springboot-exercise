package nc.solon.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** The type Tax batch in dto. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxBatchInDTO {
  @NotEmpty @Valid private List<TaxInDTO> batch;
}
