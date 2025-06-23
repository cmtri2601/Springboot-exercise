package nc.solon.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The type Person in dto. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonInDTO {

  @NotBlank(message = "First name is mandatory")
  private String firstName;

  @NotBlank(message = "Last name is mandatory")
  private String lastName;

  @NotNull(message = "Date of birth is mandatory")
  @Past(message = "Date of birth must be in the past")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private LocalDate dateOfBirth;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true, message = "Tax debt cannot be negative")
  private BigDecimal taxDebt;
}
