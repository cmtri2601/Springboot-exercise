package nc.solon.person.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.service.PersonService;
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
  @PostMapping
  public ResponseEntity<Void> createPerson(@Valid @RequestBody PersonInDTO personInDTO) {
    personService.createPerson(personInDTO);
    return ResponseEntity.accepted().build();
  }

  /**
   * Update person response entity.
   *
   * @param id the id
   * @param personInDTO the person in dto
   * @return the response entity
   */
  @PatchMapping("/{id}")
  public ResponseEntity<Void> updatePerson(
      @PathVariable Long id, @Valid @RequestBody PersonInDTO personInDTO) {
    personService.updatePerson(id, personInDTO);
    return ResponseEntity.accepted().build();
  }

  /**
   * Delete person response entity.
   *
   * @param id the id
   * @return the response entity
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
    personService.deletePerson(id);
    return ResponseEntity.accepted().build();
  }

  /**
   * Gets by id.
   *
   * @param id the id
   * @return the by id
   */
  @GetMapping("/{id}")
  public ResponseEntity<PersonOutDTO> getById(@PathVariable Long id) {
    return personService
        .getPersonById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Gets by tax id.
   *
   * @param taxId the tax id
   * @return the by tax id
   */
  @GetMapping("/tax-id/{taxId}")
  public ResponseEntity<PersonOutDTO> getByTaxId(@PathVariable String taxId) {
    return personService
        .getPersonByTaxId(taxId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Gets all persons.
   *
   * @return the all persons
   */
  @GetMapping
  public ResponseEntity<List<PersonOutDTO>> getAllPersons() {
    return ResponseEntity.ok(personService.getAllPersons());
  }

  /**
   * Find by name prefix and min age response entity.
   *
   * @param prefix the prefix
   * @param age the age
   * @return the response entity
   */
  @GetMapping("/search")
  public ResponseEntity<List<PersonOutDTO>> findByNamePrefixAndMinAge(
      @RequestParam(defaultValue = "Mi") String prefix,
      @RequestParam(defaultValue = "30") int age) {
    return ResponseEntity.ok(personService.findPersonsByNamePrefixAndMinAge(prefix, age));
  }
}
