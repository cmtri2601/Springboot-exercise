package nc.solon.person.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.dto.PersonOutDTO;
import nc.solon.common.response.ApiResponse;
import nc.solon.person.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Person controller.
 */
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
    public ResponseEntity<ApiResponse<Void>> createPerson(@Valid @RequestBody PersonInDTO personInDTO) {
        personService.createPerson(personInDTO);
        return ApiResponse.responseAccepted();
    }

    /**
     * Update person response entity.
     *
     * @param id          the id
     * @param personInDTO the person in dto
     * @return the response entity
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePerson(
            @PathVariable Long id, @Valid @RequestBody PersonInDTO personInDTO) {
        personService.updatePerson(id, personInDTO);
        return ApiResponse.responseAccepted();
    }

    /**
     * Delete person response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ApiResponse.responseAccepted();
    }

    /**
     * Gets by id.
     *
     * @param id the id
     * @return the by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonOutDTO>> getById(@PathVariable Long id) throws Exception {
        return personService
                .getPersonById(id)
                .map(ApiResponse::responseSuccess)
                .orElseGet(ApiResponse::responseNotFound);
    }

    /**
     * Gets by tax id.
     *
     * @param taxId the tax id
     * @return the by tax id
     */
    @GetMapping("/tax-id/{taxId}")
    public ResponseEntity<ApiResponse<PersonOutDTO>> getByTaxId(@PathVariable String taxId) {
        return personService
                .getPersonByTaxId(taxId)
                .map(ApiResponse::responseSuccess)
                .orElseGet(ApiResponse::responseNotFound);
    }

    /**
     * Gets all persons.
     *
     * @return the all persons
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PersonOutDTO>>> getAllPersons() {
        return ApiResponse.responseSuccess(personService.getAllPersons());
    }

    /**
     * Find by name prefix and min age response entity.
     *
     * @param prefix the prefix
     * @param age    the age
     * @return the response entity
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PersonOutDTO>>> findByNamePrefixAndMinAge(
            @RequestParam(defaultValue = "Mi") String prefix,
            @RequestParam(defaultValue = "30") int age) {
        return ApiResponse.responseSuccess(personService.findPersonsByNamePrefixAndMinAge(prefix, age));
    }
}
