package nc.solon.cron;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConsumeTaxCalculation {

    private final RestTemplate restTemplate;

    public ConsumeTaxCalculation(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    //    @Scheduled(fixedRate = 5 * 60 * 1000) // every 5 minutes
    @Scheduled(fixedRate = 5 * 1000) // every 5 s
    public void callRestEndpoint() {
        String url = "http://localhost:8080/api/v1/tax-calculation/consume-manual";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("Called endpoint. Status: " + response.getStatusCode());
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Failed to call endpoint: " + e.getMessage());
        }
    }
}
