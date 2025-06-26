package nc.solon.camunda.service;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nc.solon.camunda.entity.Person;
import nc.solon.camunda.kafka.camunda.CamundaProducer;
import nc.solon.camunda.repository.PersonRepository;
import nc.solon.camunda.utils.MapToDTO;
import nc.solon.camunda.utils.TaxIdGenerator;
import nc.solon.common.constant.Camunda;
import nc.solon.common.constant.ErrorMessage;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.dto.PersonOutDTO;
import nc.solon.common.dto.TaxInDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * The type Person service.
 */
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final TaxIdGenerator taxIdGenerator;
    private final ZeebeClient zeebeClient;
    private final CamundaProducer camundaProducer;
    private final PersonFallbackService personFallbackService;

    /**
     * Create person.
     *
     * @param dto the dto
     * @return the person out dto
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

    /**
     * Approve.
     *
     * @param id id of new user
     */
    public void approve(String id) {
        zeebeClient.newPublishMessageCommand()
                .messageName(Camunda.Messages.APPROVE)
                .correlationKey(id)
                .variables(Map.of(Camunda.Variables.STATUS, Camunda.Messages.APPROVE))
                .send()
                .join();
    }

    /**
     * Reject.
     *
     * @param id id of new user
     */
    public void reject(String id) {
        zeebeClient.newPublishMessageCommand()
                .messageName(Camunda.Messages.REJECT)
                .correlationKey(id)
                .send()
                .join();
    }

    /**
     * Add tax.
     *
     * @param dto the dto
     * @return the person out dto
     * @throws Exception the exception
     */
    @Transactional
    public PersonOutDTO addTax(TaxInDTO dto) throws Exception {
        try {
            Person existedPerson = personRepository.findByTaxId(dto.getTaxId()).orElseThrow(
                    () ->
                            new EntityNotFoundException(
                                    ErrorMessage.PERSON_NOT_FOUND_WITH_TAX_ID + dto.getTaxId()));

            // simulate error
            if (Objects.equals(existedPerson.getLastName(), String.valueOf(HttpStatus.BAD_REQUEST.value()))) {
                // Error 400 - MALFORMED_JSON
                throw new HttpMessageNotReadableException("");
            }
            if (Objects.equals(existedPerson.getLastName(), String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))) {
                // Error 500 - UNEXPECTED_ERROR
                throw new Exception();
            }

            existedPerson.setTaxDebt(existedPerson.getTaxDebt().add(dto.getAmount()));
            personRepository.save(existedPerson);
            return MapToDTO.person(existedPerson);
        } catch (Exception error) {
            personFallbackService.deleteAndLog(dto, Camunda.KafkaLogMessages.ADD_TAX_FAIL);
            throw error;
        }
    }
}
