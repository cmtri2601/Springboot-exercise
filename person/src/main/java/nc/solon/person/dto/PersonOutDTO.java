package nc.solon.person.dto;

import java.math.BigDecimal;
import lombok.*;

/** The type Person out dto. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonOutDTO {

  private Long id;

  private String firstName;

  private String lastName;

  // Exposed in API instead of dateOfBirth
  private Integer age;

  private String taxId;

  private BigDecimal taxDebt;
}
