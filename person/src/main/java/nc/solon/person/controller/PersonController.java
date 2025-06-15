package nc.solon.person.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<Void> createPerson(@Valid  @RequestBody PersonInDTO personInDTO) throws JsonProcessingException {
        personService.createPerson(personInDTO);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updatePerson(@PathVariable Long id,
                                                     @Valid @RequestBody PersonInDTO personInDTO) throws JsonProcessingException {
        personService.updatePerson(id, personInDTO);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonOutDTO> getById(@PathVariable Long id) {
        return personService.getPersonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tax-id/{taxId}")
    public ResponseEntity<PersonOutDTO> getByTaxId(@PathVariable String taxId) {
        return personService.getPersonByTaxId(taxId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PersonOutDTO>> getAllPersons() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PersonOutDTO>> findByNamePrefixAndMinAge(
            @RequestParam(defaultValue = "Mi") String prefix,
            @RequestParam(defaultValue = "30") int age) {
        return ResponseEntity.ok(personService.findPersonsByNamePrefixAndMinAge(prefix, age));
    }
}
