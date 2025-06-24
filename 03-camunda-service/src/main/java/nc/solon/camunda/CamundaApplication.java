package nc.solon.camunda;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/** The type Camunda application. */
@SpringBootApplication
@ComponentScan(basePackages = {"nc.solon.camunda", "nc.solon.common"})
@Deployment(resources = "classpath*:/bpmn/**/*.bpmn")
public class CamundaApplication {
  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CamundaApplication.class, args);
  }
}
