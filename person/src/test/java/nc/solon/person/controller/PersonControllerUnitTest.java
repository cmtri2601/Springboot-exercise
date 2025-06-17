package nc.solon.person.controller;

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
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class PersonControllerUnitTest {

  private MockMvc mockMvc;

  @Mock private PersonService personService;

  @InjectMocks private PersonController personController;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(personController).build();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void createPerson() throws Exception {
    // Arrange
    PersonInDTO personInDTO = createSamplePersonInDTO();
    doNothing().when(personService).createPerson(any(PersonInDTO.class));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(personInDTO)))
        .andExpect(status().isAccepted());

    // Verify
    verify(personService, times(1)).createPerson(any(PersonInDTO.class));
  }

  @Test
  void updatePerson() throws Exception {
    // Arrange
    Long personId = 1L;
    PersonInDTO personInDTO = createSamplePersonInDTO();
    doNothing().when(personService).updatePerson(eq(personId), any(PersonInDTO.class));

    // Act & Assert
    mockMvc
        .perform(
            patch("/api/v1/persons/{id}", personId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(personInDTO)))
        .andExpect(status().isAccepted());

    // Verify
    verify(personService, times(1)).updatePerson(eq(personId), any(PersonInDTO.class));
  }

  @Test
  void deletePerson() throws Exception {
    // Arrange
    Long personId = 1L;
    doNothing().when(personService).deletePerson(personId);

    // Act & Assert
    mockMvc.perform(delete("/api/v1/persons/{id}", personId)).andExpect(status().isAccepted());

    // Verify
    verify(personService, times(1)).deletePerson(personId);
  }

  @Test
  void getById_ExistingPerson_ReturnsOk() throws Exception {
    // Arrange
    Long personId = 1L;
    PersonOutDTO personOutDTO = createSamplePersonOutDTO();
    when(personService.getPersonById(personId)).thenReturn(Optional.of(personOutDTO));

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/persons/{id}", personId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(personOutDTO.getId()))
        .andExpect(jsonPath("$.firstName").value(personOutDTO.getFirstName()))
        .andExpect(jsonPath("$.lastName").value(personOutDTO.getLastName()))
        .andExpect(jsonPath("$.age").value(personOutDTO.getAge()));

    // Verify
    verify(personService, times(1)).getPersonById(personId);
  }

  @Test
  void getById_NonExistingPerson_ReturnsNotFound() throws Exception {
    // Arrange
    Long personId = 999L;
    when(personService.getPersonById(personId)).thenReturn(Optional.empty());

    // Act & Assert
    mockMvc.perform(get("/api/v1/persons/{id}", personId)).andExpect(status().isNotFound());

    // Verify
    verify(personService, times(1)).getPersonById(personId);
  }

  @Test
  void getByTaxId_ExistingPerson_ReturnsOk() throws Exception {
    // Arrange
    String taxId = "123456789";
    PersonOutDTO personOutDTO = createSamplePersonOutDTO();
    when(personService.getPersonByTaxId(taxId)).thenReturn(Optional.of(personOutDTO));

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/persons/tax-id/{taxId}", taxId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(personOutDTO.getId()))
        .andExpect(jsonPath("$.firstName").value(personOutDTO.getFirstName()))
        .andExpect(jsonPath("$.lastName").value(personOutDTO.getLastName()));

    // Verify
    verify(personService, times(1)).getPersonByTaxId(taxId);
  }

  @Test
  void getByTaxId_NonExistingPerson_ReturnsNotFound() throws Exception {
    // Arrange
    String taxId = "999999999";
    when(personService.getPersonByTaxId(taxId)).thenReturn(Optional.empty());

    // Act & Assert
    mockMvc.perform(get("/api/v1/persons/tax-id/{taxId}", taxId)).andExpect(status().isNotFound());

    // Verify
    verify(personService, times(1)).getPersonByTaxId(taxId);
  }

  @Test
  void getAllPersons() throws Exception {
    // Arrange
    List<PersonOutDTO> persons =
        Arrays.asList(createSamplePersonOutDTO(), createSamplePersonOutDTO(2L, "Jane", "Doe", 28));
    when(personService.getAllPersons()).thenReturn(persons);

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/persons"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(persons.get(0).getId()))
        .andExpect(jsonPath("$[0].firstName").value(persons.get(0).getFirstName()))
        .andExpect(jsonPath("$[1].id").value(persons.get(1).getId()))
        .andExpect(jsonPath("$[1].firstName").value(persons.get(1).getFirstName()));

    // Verify
    verify(personService, times(1)).getAllPersons();
  }

  @Test
  void findByNamePrefixAndMinAge() throws Exception {
    // Arrange
    String prefix = "Jo";
    int minAge = 30;
    List<PersonOutDTO> persons =
        Arrays.asList(
            createSamplePersonOutDTO(), createSamplePersonOutDTO(3L, "John", "Smith", 35));
    when(personService.findPersonsByNamePrefixAndMinAge(prefix, minAge)).thenReturn(persons);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/v1/persons/search")
                .param("prefix", prefix)
                .param("age", String.valueOf(minAge)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(persons.get(0).getId()))
        .andExpect(jsonPath("$[0].firstName").value(persons.get(0).getFirstName()))
        .andExpect(jsonPath("$[1].id").value(persons.get(1).getId()))
        .andExpect(jsonPath("$[1].firstName").value(persons.get(1).getFirstName()));

    // Verify
    verify(personService, times(1)).findPersonsByNamePrefixAndMinAge(prefix, minAge);
  }

  // Helper methods for creating test data
  private PersonInDTO createSamplePersonInDTO() {
    return PersonInDTO.builder()
        .firstName("John")
        .lastName("Doe")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .taxDebt(BigDecimal.valueOf(100.00))
        .build();
  }

  private PersonOutDTO createSamplePersonOutDTO() {
    return createSamplePersonOutDTO(1L, "John", "Doe", 33);
  }

  private PersonOutDTO createSamplePersonOutDTO(
      Long id, String firstName, String lastName, int age) {
    return PersonOutDTO.builder()
        .id(id)
        .firstName(firstName)
        .lastName(lastName)
        .age(age)
        .taxId("123456789")
        .taxDebt(BigDecimal.valueOf(100.00))
        .build();
  }
}
