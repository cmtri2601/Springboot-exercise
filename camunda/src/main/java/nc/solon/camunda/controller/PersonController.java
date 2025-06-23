package nc.solon.camunda.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.camunda.service.PersonService;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.dto.PersonOutDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** The type Person controller. */
@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
public class PersonController {

  private final PersonService personService;

  /**
   * Create person response entity.
   *
   * @param personInDTO the person in dto
   * @return the response entity
   */
  @PostMapping()
  public ResponseEntity<PersonOutDTO> createPersonSync(
      @Valid @RequestBody PersonInDTO personInDTO) {
    PersonOutDTO personOutDTO = personService.createPerson(personInDTO);
    return ResponseEntity.ok(personOutDTO);
  }
}
