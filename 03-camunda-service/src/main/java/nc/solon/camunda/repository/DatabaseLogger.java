package nc.solon.camunda.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** The type Database logger. */
@Slf4j
@Component
public class DatabaseLogger {

  @Value("${spring.datasource.url}")
  private String datasourceUrl;

  @Value("${spring.datasource.username}")
  private String datasourceUsername;

  /** Log database info. */
  @PostConstruct
  public void logDatabaseInfo() {
    log.info("🔗 PostgreSQL JDBC URL: {}", datasourceUrl);
    log.info("👤 PostgreSQL username: {}", datasourceUsername);
  }
}
