package nc.solon.camunda;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath:sample.bpmn")
public class CamundaApplication {
	public static void main(String[] args) {
		SpringApplication.run(CamundaApplication.class, args);
	}

}
