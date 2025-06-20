package nc.solon.camunda.service;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import java.time.Instant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CamundaService {

    @JobWorker(type = "foo", autoComplete = true) // autoComplete = true as default value
    public void handleFooJob(final ActivatedJob job) {
        logJob(job, "hello foo");
    }

    @JobWorker(type = "bar", autoComplete = true) // autoComplete = true as default value
    public void handleBarJob(final ActivatedJob job) {
        logJob(job, "hello bar");
    }

    @JobWorker(type = "create-person", autoComplete = true) // autoComplete = true as default value
    public void handleCreatePersonJob(final ActivatedJob job) {
        logJob(job, "hello bar");
    }

    private static void logJob(final ActivatedJob job, Object parameterValue) {
        log.info(
                "complete job\n>>> [type: {}, key: {}, element: {}, workflow instance: {}]\n{deadline; {}]\n[headers: {}]\n[variable parameter: {}\n[variables: {}]",
                job.getType(),
                job.getKey(),
                job.getElementId(),
                job.getProcessInstanceKey(),
                Instant.ofEpochMilli(job.getDeadline()),
                job.getCustomHeaders(),
                parameterValue,
                job.getVariables());
    }
}