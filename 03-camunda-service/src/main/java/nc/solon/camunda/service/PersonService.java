package nc.solon.camunda.service;

import lombok.RequiredArgsConstructor;
import nc.solon.camunda.entity.Person;
import nc.solon.camunda.repository.PersonRepository;
import nc.solon.camunda.utils.MapToDTO;
import nc.solon.camunda.utils.TaxIdGenerator;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.dto.PersonOutDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** The type Person service. */
@Service
@RequiredArgsConstructor
public class PersonService {

  private final PersonRepository personRepository;
  private final TaxIdGenerator taxIdGenerator;

  /**
   * Create person sync.
   *
   * @param dto the dto
   */
  public PersonOutDTO createPerson(PersonInDTO dto) {
    Person person =
        Person.builder()
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .dateOfBirth(dto.getDateOfBirth())
            .taxId(taxIdGenerator.generateTaxId())
            .taxDebt(BigDecimal.ZERO)
            .build();
    Person entity = personRepository.save(person);
    return MapToDTO.person(entity);
  }
}
