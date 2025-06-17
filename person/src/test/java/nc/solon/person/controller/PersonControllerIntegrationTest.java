package nc.solon.person.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Transactional
public class PersonControllerIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

  @Container @ServiceConnection
  static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka"));

  @DynamicPropertySource
  static void overrideKafkaProps(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired private TestRestTemplate restTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  private static final Duration KAFKA_TIMEOUT = Duration.ofMillis(5000);

  @Test
  void testGetAllPersons() {
    ResponseEntity<PersonOutDTO[]> response =
        restTemplate.getForEntity("/api/v1/persons", PersonOutDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(Objects.requireNonNull(response.getBody()).length > 0);
  }

  @Test
  void testGetPersonById() {
    ResponseEntity<PersonOutDTO> response =
        restTemplate.getForEntity("/api/v1/persons/1", PersonOutDTO.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1L, response.getBody().getId());
    assertNotNull(response.getBody().getFirstName());
  }

  @Test
  void testGetPersonById_NotFound() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/api/v1/persons/999", String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testCreatePerson() {
    PersonInDTO personIn =
        new PersonInDTO("Test", "User", LocalDate.of(1990, 5, 15), new BigDecimal("4500.00"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonInDTO> request = new HttpEntity<>(personIn, headers);

    ResponseEntity<PersonOutDTO> response =
        restTemplate.postForEntity("/api/v1/persons", request, PersonOutDTO.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

    // Wait for Kafka event to be processed
    Awaitility.await()
        .atMost(KAFKA_TIMEOUT)
        .untilAsserted(
            () -> {
              ResponseEntity<PersonOutDTO> verifyResponse =
                  restTemplate.getForEntity("/api/v1/persons/5", PersonOutDTO.class);
              assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
              assertEquals("Test", Objects.requireNonNull(verifyResponse.getBody()).getFirstName());
              assertEquals("User", verifyResponse.getBody().getLastName());
            });
  }

  @Test
  void testUpdatePerson() {
    PersonInDTO updateData =
        new PersonInDTO("Updated", "Person", LocalDate.of(1995, 10, 20), new BigDecimal("5500.00"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonInDTO> request = new HttpEntity<>(updateData, headers);

    ResponseEntity<PersonOutDTO> response =
        restTemplate.exchange("/api/v1/persons/2", HttpMethod.PATCH, request, PersonOutDTO.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

    // Wait for Kafka event to be processed
    Awaitility.await()
        .atMost(KAFKA_TIMEOUT)
        .untilAsserted(
            () -> {
              ResponseEntity<PersonOutDTO> verifyResponse =
                  restTemplate.getForEntity("/api/v1/persons/2", PersonOutDTO.class);
              assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
              assertEquals(
                  "Updated", Objects.requireNonNull(verifyResponse.getBody()).getFirstName());
              assertEquals("Person", verifyResponse.getBody().getLastName());
            });
  }

  @Test
  void testUpdatePerson_NotFound() {
    PersonInDTO updateData =
        new PersonInDTO("Updated", "Person", LocalDate.of(1995, 10, 20), new BigDecimal("5500.00"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonInDTO> request = new HttpEntity<>(updateData, headers);

    ResponseEntity<String> response =
        restTemplate.exchange("/api/v1/persons/999", HttpMethod.PATCH, request, String.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
  }

  @Test
  void testDeletePerson() {
    // First, make sure the person exists
    ResponseEntity<PersonOutDTO> checkResponse =
        restTemplate.getForEntity("/api/v1/persons/1", PersonOutDTO.class);
    assertEquals(HttpStatus.OK, checkResponse.getStatusCode());

    // Delete the person
    ResponseEntity<Void> deleteResponse =
        restTemplate.exchange("/api/v1/persons/1", HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.ACCEPTED, deleteResponse.getStatusCode());

    // Wait for Kafka event to be processed
    Awaitility.await()
        .atMost(KAFKA_TIMEOUT)
        .untilAsserted(
            () -> {
              ResponseEntity<String> verifyResponse =
                  restTemplate.getForEntity("/api/v1/persons/1", String.class);
              assertEquals(HttpStatus.NOT_FOUND, verifyResponse.getStatusCode());
            });
  }

  @Test
  void testInvalidInput_BadRequest() {
    PersonInDTO invalidPerson = new PersonInDTO("", "", null, new BigDecimal("-100.00"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonInDTO> request = new HttpEntity<>(invalidPerson, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/v1/persons", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
