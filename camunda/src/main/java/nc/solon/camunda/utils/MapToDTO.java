package nc.solon.camunda.utils;

import nc.solon.camunda.entity.Person;
import nc.solon.common.dto.PersonOutDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

/** The type Map to dto. */
@Component
public class MapToDTO {

  /**
   * Person out dto.
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
