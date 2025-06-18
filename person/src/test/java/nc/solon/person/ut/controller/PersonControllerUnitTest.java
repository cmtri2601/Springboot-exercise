package nc.solon.person.ut.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import nc.solon.person.controller.PersonController;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** The type Person controller unit test. */
@ExtendWith(MockitoExtension.class)
class PersonControllerUnitTest {

  @Mock private PersonService personService;

  @InjectMocks private PersonController personController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  /** Sets up. */
@BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(personController).build();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  /**
   * Gets all persons should return list of persons.
   *
   * @throws Exception the exception
   */
@Test
  void getAllPersons_ShouldReturnListOfPersons() throws Exception {
    // Given
    List<PersonOutDTO> persons =
        Arrays.asList(
            new PersonOutDTO(1L, "John", "Doe", 25, "123456789", new BigDecimal(33)),
            new PersonOutDTO(2L, "Jane", "Smith", 30, "987654321", new BigDecimal(38)));
    when(personService.getAllPersons()).thenReturn(persons);

    // When/Then
    mockMvc
        .perform(get("/persons"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].firstName").value("John"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].firstName").value("Jane"));

    verify(personService).getAllPersons();
  }

  /**
   * Gets person by id with existing id should return person.
   *
   * @throws Exception the exception
   */
@Test
  void getPersonById_WithExistingId_ShouldReturnPerson() throws Exception {
    // Given
    PersonOutDTO person = new PersonOutDTO(1L, "John", "Doe", 25, "123456789", new BigDecimal(33));
    when(personService.getPersonById(1L)).thenReturn(Optional.of(person));

    // When/Then
    mockMvc
        .perform(get("/persons/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.firstName").value("John"));

    verify(personService).getPersonById(1L);
  }

  /**
   * Gets person by id with non existing id should return 404.
   *
   * @throws Exception the exception
   */
@Test
  void getPersonById_WithNonExistingId_ShouldReturn404() throws Exception {
    // Given
    when(personService.getPersonById(999L)).thenReturn(Optional.empty());

    // When/Then
    mockMvc.perform(get("/persons/999")).andExpect(status().isNotFound());

    verify(personService).getPersonById(999L);
  }

  /**
   * Create person with valid data should return accepted.
   *
   * @throws Exception the exception
   */
@Test
  void createPerson_WithValidData_ShouldReturnAccepted() throws Exception {
    // Given
    PersonInDTO personIn =
        new PersonInDTO("John", "Doe", LocalDate.of(1990, 1, 1), new BigDecimal("5000.00"));

    // When/Then
    mockMvc
        .perform(
            post("/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(personIn)))
        .andExpect(status().isAccepted());

    verify(personService).createPerson(any(PersonInDTO.class));
  }

  /**
   * Create person with invalid data should return bad request.
   *
   * @throws Exception the exception
   */
@Test
  void createPerson_WithInvalidData_ShouldReturnBadRequest() throws Exception {
    // Given
    PersonInDTO invalidPerson = new PersonInDTO("", "", null, new BigDecimal("-100.00"));

    // When/Then
    mockMvc
        .perform(
            post("/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPerson)))
        .andExpect(status().isBadRequest());

    verify(personService, never()).createPerson(any(PersonInDTO.class));
  }

  /**
   * Update person with valid data should return accepted.
   *
   * @throws Exception the exception
   */
@Test
  void updatePerson_WithValidData_ShouldReturnAccepted() throws Exception {
    // Given
    PersonInDTO updateDto =
        new PersonInDTO("Updated", "Person", LocalDate.of(1995, 10, 20), new BigDecimal("6000.00"));

    // When/Then
    mockMvc
        .perform(
            patch("/persons/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isAccepted());

    verify(personService).updatePerson(eq(1L), any(PersonInDTO.class));
  }

  /**
   * Delete person should call service and return no content.
   *
   * @throws Exception the exception
   */
@Test
  void deletePerson_ShouldCallServiceAndReturnNoContent() throws Exception {
    // When/Then
    mockMvc.perform(delete("/persons/1")).andExpect(status().isAccepted());

    verify(personService).deletePerson(1L);
  }

  /**
   * Gets person by tax id with existing tax id should return person.
   *
   * @throws Exception the exception
   */
@Test
  void getPersonByTaxId_WithExistingTaxId_ShouldReturnPerson() throws Exception {
    // Given
    PersonOutDTO person = new PersonOutDTO(1L, "John", "Doe", 25, "TID12345", new BigDecimal(33));
    when(personService.getPersonByTaxId("TID12345")).thenReturn(Optional.of(person));

    // When/Then
    mockMvc
        .perform(get("/persons/tax-id/TID12345"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.taxId").value("TID12345"));

    verify(personService).getPersonByTaxId("TID12345");
  }

  /**
   * Gets person by tax id with non existing tax id should return 404.
   *
   * @throws Exception the exception
   */
@Test
  void getPersonByTaxId_WithNonExistingTaxId_ShouldReturn404() throws Exception {
    // Given
    when(personService.getPersonByTaxId("INVALID")).thenReturn(Optional.empty());

    // When/Then
    mockMvc.perform(get("/persons/tax-id/INVALID")).andExpect(status().isNotFound());

    verify(personService).getPersonByTaxId("INVALID");
  }

  /**
   * Find by name prefix and min age should return matching persons.
   *
   * @throws Exception the exception
   */
@Test
  void findByNamePrefixAndMinAge_ShouldReturnMatchingPersons() throws Exception {
    // Given
    List<PersonOutDTO> persons =
        Arrays.asList(
            new PersonOutDTO(1L, "John", "Doe", 30, "123456789", new BigDecimal(33)),
            new PersonOutDTO(2L, "Jonathan", "Smith", 35, "987654321", new BigDecimal(38)));
    when(personService.findPersonsByNamePrefixAndMinAge("Jo", 30)).thenReturn(persons);

    // When/Then
    mockMvc
        .perform(get("/persons/search").param("prefix", "Jo").param("minAge", "30"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].firstName").value("John"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].firstName").value("Jonathan"));

    verify(personService).findPersonsByNamePrefixAndMinAge("Jo", 30);
  }
}
