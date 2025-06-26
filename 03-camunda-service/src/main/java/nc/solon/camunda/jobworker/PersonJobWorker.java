package nc.solon.camunda.jobworker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.camunda.kafka.camunda.CamundaProducer;
import nc.solon.camunda.service.PersonService;
import nc.solon.camunda.utils.Log;
import nc.solon.common.constant.Camunda;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.dto.PersonOutDTO;
import nc.solon.common.event.CamundaLogEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Person job worker.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonJobWorker {

    private final PersonService personService;
    private final CamundaProducer camundaProducer;
    private final ZeebeClient zeebe;

    /**
     * Handle create person job.
     *
     * @param client the client
     * @param job    the job
     * @return the map
     */
    @JobWorker(type = Camunda.Types.CREATE_PERSON) // autoComplete = true as default value
    public Map<String, Object> handleCreatePersonJob(JobClient client, final ActivatedJob job) {
        try {
            var dto = job.getVariablesAsType(PersonInDTO.class);
            PersonOutDTO result = personService.createPerson(dto);
            Log.jobWorker(job, Camunda.Types.CREATE_PERSON);
            var variables = new HashMap<String, Object>();
            variables.put(Camunda.Variables.PERSON_ID, result.getId());
            return variables;
        } catch (Exception e) {
            zeebe.newSetVariablesCommand(job.getElementInstanceKey())
                    .variables(Map.of(Camunda.Variables.ERROR_MESSAGE, e.getMessage()))
                    .send();
            throw new ZeebeBpmnError(Camunda.Errors.CREATE_PERSON_FAIL, e.getMessage());
        }
    }

    /**
     * Handle create person fail job.
     *
     * @param job the job
     */
    @JobWorker(type = Camunda.Types.CREATE_PERSON_FAIL) // autoComplete = true as default value
    public void handleCreatePersonFailJob(final ActivatedJob job) {
        Map<String, Object> data = job.getVariablesAsMap();
        String errorMessage = (String) data.get(Camunda.Variables.ERROR_MESSAGE);
        data.remove(Camunda.Variables.ERROR_MESSAGE);
        var event = new CamundaLogEvent(Camunda.KafkaLogMessages.CREATE_PERSON_FAIL, errorMessage, data);
        camundaProducer.sendEvent(event);
        Log.jobWorker(job, Camunda.Types.CREATE_PERSON_FAIL);
    }
}
