package nc.solon.person.utils;

import java.time.LocalDate;
import java.time.Period;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.entity.Person;
import org.springframework.stereotype.Component;

/** The type Map to dto. */
@Component
public class MapToDTO {

  /**
   * Person person out dto.
   *
   * @param person the person
   * @return the person out dto
   */
  public static PersonOutDTO person(Person person) {
    return PersonOutDTO.builder()
        .id(person.getId())
        .firstName(person.getFirstName())
        .lastName(person.getLastName())
        .taxId(person.getTaxId())
        .age(Period.between(person.getDateOfBirth(), LocalDate.now()).getYears())
        .taxDebt(person.getTaxDebt())
        .build();
  }
}
