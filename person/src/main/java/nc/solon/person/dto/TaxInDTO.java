package nc.solon.person.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** The type Tax in dto. */
@Getter
@Setter
public class TaxInDTO {
  @NotBlank(message = "Tax id is mandatory")
  private String taxId;

  @NotNull(message = "Tax amount is mandatory")
  private BigDecimal amount;
}
