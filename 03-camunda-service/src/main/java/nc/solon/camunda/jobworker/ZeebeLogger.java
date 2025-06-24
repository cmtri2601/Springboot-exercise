package nc.solon.camunda.jobworker;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** The type Zeebe logger. */
@Slf4j
@Component
public class ZeebeLogger {

  @Value("${camunda.client.zeebe.base-url}")
  private String zeebeBaseUrl;

  /** Log zeebe url. */
  @PostConstruct
  public void logZeebeUrl() {
    log.info("🔗 Zeebe base URL: {}", zeebeBaseUrl);
  }
}
