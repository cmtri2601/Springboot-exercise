package nc.solon.camunda.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.solon.camunda.service.PersonService;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.dto.PersonOutDTO;
import nc.solon.common.dto.TaxInDTO;
import nc.solon.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping()
    public ResponseEntity<ApiResponse<PersonOutDTO>> createPerson(
            @Valid @RequestBody PersonInDTO personInDTO) {
        PersonOutDTO personOutDTO = personService.createPerson(personInDTO);
        return ApiResponse.responseSuccess(personOutDTO);
    }

    /**
     * Approve camunda event.
     *
     * @param id id of person
     * @return the response entity
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable String id) {
        personService.approve(id);
        return ApiResponse.responseSuccess(null);
    }

    /**
     * Reject camunda event.
     *
     * @param id id of person
     * @return the response entity
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable String id) {
        personService.reject(id);
        return ApiResponse.responseSuccess(null);
    }

    /**
     * Add tax entity.
     *
     * @param dto the dto
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping("/tax")
    public ResponseEntity<ApiResponse<PersonOutDTO>> addTax(
            @Valid @RequestBody TaxInDTO dto) throws Exception {
        PersonOutDTO personOutDTO = personService.addTax(dto);
        return ApiResponse.responseSuccess(personOutDTO);
    }
}
