package nc.solon.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** The type Tax in dto. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxInDTO {
  @NotBlank(message = "Tax id is mandatory")
  @Size(max = 10, message = "Tax id must not exceed 10 characters")
  private String taxId;

  @NotNull(message = "Tax amount is mandatory")
  private BigDecimal amount;
}
