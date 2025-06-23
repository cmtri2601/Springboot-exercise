package nc.solon.camunda;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"nc.solon.camunda",
		"nc.solon.common"
})
@Deployment(resources = "classpath*:/bpmn/**/*.bpmn")
public class CamundaApplication {
	public static void main(String[] args) {
		SpringApplication.run(CamundaApplication.class, args);
	}
}
