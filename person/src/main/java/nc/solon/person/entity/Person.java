package nc.solon.person.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    @NotBlank
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    @Past
    @NotNull
    private LocalDate dateOfBirth;

    @Column(name = "tax_id", nullable = false, unique = true, updatable = false)
    @NotBlank
    private String taxId;

    @Column(name = "tax_debt", nullable = false)
    @NotNull
    private BigDecimal taxDebt;
}
