package nc.solon.cron.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ConsumeTaxCalculation {

    private final RestTemplate restTemplate;

    @Value("${person-service.consume-url}")
    String consumeUrl;

    public ConsumeTaxCalculation(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Scheduled(fixedRateString = "${fixed-rate.consume-tax-calculation}")
    public void callRestEndpoint() {

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(consumeUrl, String.class);
            log.info("Called endpoint. Status: {}", response.getStatusCode());
            log.info("Response: {}", response.getBody());
        } catch (Exception e) {
            log.info("Failed to call endpoint: {}", e.getMessage());
        }
    }
}
