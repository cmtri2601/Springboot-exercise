package nc.solon.person.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import nc.solon.person.entity.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** The type Person repository integration test. */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class PersonRepositoryIntegrationTest {

  /** The constant postgres. */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Autowired private PersonRepository personRepository;

  /** Find by name prefix and older than should return matching persons. */
  @Test
  void findByNamePrefixAndOlderThan_shouldReturnMatchingPersons() {
    // Arrange
    String namePrefix = "Mi";
    LocalDate maxBirthDate = LocalDate.of(1985, 1, 1); // Older than 1985-01-01

    // Act
    List<Person> result = personRepository.findByNamePrefixAndOlderThan(namePrefix, maxBirthDate);

    // Assert
    assertEquals(1, result.size());
    Person matchedPerson = result.get(0);
    assertEquals("Michael", matchedPerson.getFirstName());
    assertEquals("Johnson", matchedPerson.getLastName());
    assertEquals(LocalDate.of(1980, 5, 15), matchedPerson.getDateOfBirth());
  }

  /** Find by name prefix and older than with last name prefix should return matching persons. */
  @Test
  void findByNamePrefixAndOlderThan_withLastNamePrefix_shouldReturnMatchingPersons() {
    // Arrange
    String namePrefix = "Mi";
    LocalDate maxBirthDate = LocalDate.of(1990, 1, 1); // Older than 1990-01-01

    // Act
    List<Person> result = personRepository.findByNamePrefixAndOlderThan(namePrefix, maxBirthDate);

    // Assert
    assertEquals(2, result.size());
    // Verify that both persons with first name or last name starting with "Mi" are returned
    boolean foundMichael = false;
    boolean foundMitchell = false;

    for (Person person : result) {
      if (person.getFirstName().equals("Michael")) {
        foundMichael = true;
      } else if (person.getLastName().equals("Mitchell")) {
        foundMitchell = true;
      }
    }

    assertTrue(foundMichael, "Person with first name 'Michael' should be found");
    assertTrue(foundMitchell, "Person with last name 'Mitchell' should be found");
  }

  /** Find by name prefix and older than with no matches should return empty list. */
  @Test
  void findByNamePrefixAndOlderThan_withNoMatches_shouldReturnEmptyList() {
    // Arrange
    String namePrefix = "Xyz";
    LocalDate maxBirthDate = LocalDate.of(1990, 1, 1);

    // Act
    List<Person> result = personRepository.findByNamePrefixAndOlderThan(namePrefix, maxBirthDate);

    // Assert
    assertTrue(result.isEmpty());
  }

  /** Find by name prefix and older than is case insensitive. */
  @Test
  void findByNamePrefixAndOlderThan_isCaseInsensitive() {
    // Arrange
    String namePrefix = "mi"; // lowercase
    LocalDate maxBirthDate = LocalDate.of(1985, 1, 1); // Older than 1985-01-01

    // Act
    List<Person> result = personRepository.findByNamePrefixAndOlderThan(namePrefix, maxBirthDate);

    // Assert
    assertEquals(1, result.size());
    Person matchedPerson = result.get(0);
    assertEquals("Michael", matchedPerson.getFirstName());
  }
}
