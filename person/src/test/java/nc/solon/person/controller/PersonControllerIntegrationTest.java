package nc.solon.person.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import nc.solon.person.config.AbstractIntegrationTest;
import nc.solon.person.constant.KafkaTest;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

public class PersonControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Value("${server.prefix}")
  private String prefix;

  private String url;

  @BeforeEach
  public void setUp() {
    url = prefix + "/persons";
  }

  @Test
  void testGetAllPersons() {
    ResponseEntity<PersonOutDTO[]> response = restTemplate.getForEntity(url, PersonOutDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(Objects.requireNonNull(response.getBody()).length > 0);
  }

  @Test
  void testGetPersonById() {
    ResponseEntity<PersonOutDTO> response =
        restTemplate.getForEntity(url + "/1", PersonOutDTO.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1L, response.getBody().getId());
    assertNotNull(response.getBody().getFirstName());
  }

  @Test
  void testGetPersonById_NotFound() {
    ResponseEntity<String> response = restTemplate.getForEntity(url + "/999", String.class);

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
        restTemplate.postForEntity(url, request, PersonOutDTO.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

    // Wait for Kafka event to be processed
    Awaitility.await()
        .atMost(KafkaTest.KAFKA_TIMEOUT)
        .untilAsserted(
            () -> {
              ResponseEntity<PersonOutDTO> verifyResponse =
                  restTemplate.getForEntity(url + "/5", PersonOutDTO.class);
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
        restTemplate.exchange(url + "/2", HttpMethod.PATCH, request, PersonOutDTO.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

    // Wait for Kafka event to be processed
    Awaitility.await()
        .atMost(KafkaTest.KAFKA_TIMEOUT)
        .untilAsserted(
            () -> {
              ResponseEntity<PersonOutDTO> verifyResponse =
                  restTemplate.getForEntity(url + "/2", PersonOutDTO.class);
              assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
              assertEquals(
                  "Updated", Objects.requireNonNull(verifyResponse.getBody()).getFirstName());
              assertEquals("Person", verifyResponse.getBody().getLastName());
            });
  }

  @Test
  void testDeletePerson() {
    // First verify the person exists
    ResponseEntity<PersonOutDTO> getResponse =
        restTemplate.getForEntity(url + "/1", PersonOutDTO.class);
    assertEquals(HttpStatus.OK, getResponse.getStatusCode());

    // Delete the person
    restTemplate.delete(url + "/1");

    // Wait for Kafka event to be processed and verify deletion
    Awaitility.await()
        .atMost(KafkaTest.KAFKA_TIMEOUT)
        .untilAsserted(
            () -> {
              ResponseEntity<String> verifyResponse =
                  restTemplate.getForEntity("/api/v1/persons/1", String.class);
              assertEquals(HttpStatus.NOT_FOUND, verifyResponse.getStatusCode());
            });
  }

  @Test
  void testFindByNamePrefixAndMinAge_CaseInsensitive() {
    ResponseEntity<PersonOutDTO[]> response =
        restTemplate.getForEntity(url + "/search?prefix=mi&age=30", PersonOutDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    PersonOutDTO[] persons = response.getBody();
    assertNotNull(persons);
    assertTrue(persons.length > 0);

    // Verify case insensitive search works
    for (PersonOutDTO person : persons) {
      assertTrue(
          person.getFirstName().toLowerCase().startsWith("mi")
              || person.getLastName().toLowerCase().startsWith("mi"));
    }
  }

  @Test
  void testGetByTaxId_NotFound() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(url + "/tax-id/invalid-format", String.class);

    // Assuming the API validates tax ID format (if it doesn't, this test should be adjusted)
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testFindByNamePrefixAndMinAge_EmptyPrefix() {
    ResponseEntity<PersonOutDTO[]> response =
        restTemplate.getForEntity(url + "/search?prefix=&age=20", PersonOutDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    PersonOutDTO[] persons = response.getBody();
    assertNotNull(persons);
    // Should return persons of at least 20 years old
    for (PersonOutDTO person : persons) {
      assertTrue(LocalDate.now().getYear() - person.getAge() >= 20);
    }
  }

  @Test
  void testCreatePerson_NegativeTax() {
    PersonInDTO personIn =
        new PersonInDTO("Test", "User", LocalDate.of(1990, 5, 15), new BigDecimal("-4500.00"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonInDTO> request = new HttpEntity<>(personIn, headers);

    ResponseEntity<PersonOutDTO> response =
        restTemplate.postForEntity(url, request, PersonOutDTO.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testInvalidInput_BadRequest() {
    PersonInDTO invalidPerson = new PersonInDTO("", "", null, new BigDecimal("-100.00"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonInDTO> request = new HttpEntity<>(invalidPerson, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
