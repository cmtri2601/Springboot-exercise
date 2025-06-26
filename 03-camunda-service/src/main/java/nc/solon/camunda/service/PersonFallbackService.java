package nc.solon.camunda.service;

import lombok.RequiredArgsConstructor;
import nc.solon.camunda.kafka.camunda.CamundaProducer;
import nc.solon.camunda.repository.PersonRepository;
import nc.solon.common.dto.TaxInDTO;
import nc.solon.common.event.CamundaLogEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Person fallback service.
 */
@Service
@RequiredArgsConstructor
public class PersonFallbackService {

    private final PersonRepository personRepository;
    private final CamundaProducer camundaProducer;

    /**
     * Delete and log.
     *
     * @param dto     the dto
     * @param message the message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAndLog(TaxInDTO dto, String message) {
        personRepository.deletePersonByTaxId(dto.getTaxId());
        var event = new CamundaLogEvent(message, null, dto);
        camundaProducer.sendEvent(event);
    }
}