package nc.solon.camunda.jobworker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.solon.camunda.service.PersonService;
import nc.solon.camunda.utils.Log;
import nc.solon.common.dto.PersonInDTO;
import nc.solon.common.dto.PersonOutDTO;
import org.springframework.stereotype.Component;

/** The type Person job worker. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonJobWorker {

  private final PersonService personService;

  /**
   * Handle create person job map.
   *
   * @param client the client
   * @param job the job
   * @return the map
   */
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
