package nc.solon.camunda.jobworker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.camunda.service.PersonService;
import nc.solon.camunda.utils.Log;
import nc.solon.common.audit.Auditable;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.dto.PersonOutDTO;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonJobWorker {

    private final PersonService personService;

    @JobWorker(type = "create-person") // autoComplete = true as default value
    public Map<String, Object> handleCreatePersonJob(final JobClient client, final ActivatedJob job) {
        Log.jobWorker(job, "create-person");
        var dto = job.getVariablesAsType(PersonInDTO.class);
        log.info(dto.toString());
        PersonOutDTO result = personService.createPerson(dto);
        var variables = new HashMap<String, Object>();
        variables.put("id", result.getId());
        return variables;
    }
}