package nc.solon.cron.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
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
            System.out.println("Called endpoint. Status: " + response.getStatusCode());
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Failed to call endpoint: " + e.getMessage());
        }
    }
}
