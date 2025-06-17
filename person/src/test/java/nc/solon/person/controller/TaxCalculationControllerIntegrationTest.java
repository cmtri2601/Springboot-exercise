package nc.solon.person.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import nc.solon.person.config.AbstractIntegrationTest;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.dto.TaxBatchInDTO;
import nc.solon.person.dto.TaxInDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

public class TaxCalculationControllerIntegrationTest extends AbstractIntegrationTest {
  @Autowired private TestRestTemplate restTemplate;

  @Value("${server.prefix}")
  private String prefix;

  private String url;

  @BeforeEach
  public void setUp() {
    url = prefix + "/tax-calculation";
  }

  @Test
  void testCalculateTax() {
    TaxInDTO taxInDTO = new TaxInDTO("TAX000001", new BigDecimal(10));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<TaxInDTO> request = new HttpEntity<>(taxInDTO, headers);

    ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
  }

  @Test
  void testCalculateTax_InvalidInput() {
    TaxInDTO taxInDTO = new TaxInDTO("TAX00000000000000001", new BigDecimal("-10.00"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<TaxInDTO> request = new HttpEntity<>(taxInDTO, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testCalculateTaxBatch() {
    List<TaxInDTO> batch =
        Arrays.asList(
            new TaxInDTO("TAX000001", new BigDecimal(10)),
            new TaxInDTO("TAX000002", new BigDecimal(10)));
    TaxBatchInDTO batchDto = new TaxBatchInDTO(batch);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<TaxBatchInDTO> request = new HttpEntity<>(batchDto, headers);

    ResponseEntity<Void> response = restTemplate.postForEntity(url + "/batch", request, Void.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
  }

  @Test
  void testCalculateTaxBatch_InvalidInput() {
    List<TaxInDTO> batch =
        Arrays.asList(
            new TaxInDTO("TAX0000000000000001", new BigDecimal(10)),
            new TaxInDTO("TAX0000000000000002", new BigDecimal(-10)));
    TaxBatchInDTO batchDto = new TaxBatchInDTO(batch);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<TaxBatchInDTO> request = new HttpEntity<>(batchDto, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(url + "/batch", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testProduceManual() {
    List<TaxInDTO> batch =
        Arrays.asList(
            new TaxInDTO("TAX000001", new BigDecimal(10)),
            new TaxInDTO("TAX000002", new BigDecimal(10)));
    TaxBatchInDTO batchDto = new TaxBatchInDTO(batch);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<TaxBatchInDTO> request = new HttpEntity<>(batchDto, headers);

    ResponseEntity<Void> response =
        restTemplate.postForEntity(url + "/manual", request, Void.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
  }

  @Test
  void testConsumeManual() {
    // First produce some tax calculations to consume
    List<TaxInDTO> batch =
        Arrays.asList(
            new TaxInDTO("TAX000001", new BigDecimal(10)),
            new TaxInDTO("TAX000002", new BigDecimal(10)));
    TaxBatchInDTO batchDto = new TaxBatchInDTO(batch);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<TaxBatchInDTO> produceRequest = new HttpEntity<>(batchDto, headers);

    restTemplate.postForEntity(url + "/manual", produceRequest, Void.class);

    // Then consume the tax calculations
    ResponseEntity<ManualConsumeTaxOutDTO> response =
        restTemplate.getForEntity(url + "/manual?count=2", ManualConsumeTaxOutDTO.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void testConsumeManual_DefaultCount() {
    // Test the default count parameter
    ResponseEntity<ManualConsumeTaxOutDTO> response =
        restTemplate.getForEntity(url + "/manual", ManualConsumeTaxOutDTO.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }
}
