package nc.solon.person.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import nc.solon.person.dto.TaxBatchInDTO;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.service.TaxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** The type Tax calculation controller unit test. */
@ExtendWith(MockitoExtension.class)
class TaxCalculationControllerUnitTest {
  @Mock private TaxService taxService;

  @InjectMocks private TaxCalculationController taxCalculationController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  private TaxInDTO validTaxInDTO;
  private TaxBatchInDTO validBatchInDTO;

  /** Sets up. */
  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(taxCalculationController).build();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    validTaxInDTO = new TaxInDTO("TAX123", new BigDecimal("100.00"));
    validBatchInDTO = new TaxBatchInDTO(List.of(validTaxInDTO));
  }

  /**
   * Calculate tax with valid input should return ok.
   *
   * @throws Exception the exception
   */
  @Test
  void calculateTax_withValidInput_shouldReturnOk() throws Exception {
    // Given
    BigDecimal calculatedTax = new BigDecimal("20.00");
    //    when(taxService.calculateTax(any(TaxInDTO.class))).thenReturn(calculatedTax);

    // When & Then
    mockMvc
        .perform(
            post("/tax-calculation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTaxInDTO)))
        .andExpect(status().isAccepted());
  }

  /**
   * Calculate tax with invalid tax id should return bad request.
   *
   * @throws Exception the exception
   */
  @Test
  void calculateTax_withInvalidTaxId_shouldReturnBadRequest() throws Exception {
    TaxInDTO invalidDTO = new TaxInDTO("", new BigDecimal("100.00"));

    mockMvc
        .perform(
            post("/tax-calculation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
        .andExpect(status().isBadRequest());
  }

  /**
   * Calculate tax with too long tax id should return bad request.
   *
   * @throws Exception the exception
   */
  @Test
  void calculateTax_withTooLongTaxId_shouldReturnBadRequest() throws Exception {
    TaxInDTO invalidDTO = new TaxInDTO("12345678901", new BigDecimal("100.00"));

    mockMvc
        .perform(
            post("/tax-calculation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
        .andExpect(status().isBadRequest());
  }

  /**
   * Calculate tax with null amount should return bad request.
   *
   * @throws Exception the exception
   */
  @Test
  void calculateTax_withNullAmount_shouldReturnBadRequest() throws Exception {
    TaxInDTO invalidDTO = new TaxInDTO("TAX123", null);

    mockMvc
        .perform(
            post("/tax-calculation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
        .andExpect(status().isBadRequest());
  }

  /**
   * Calculate tax with negative amount should return bad request.
   *
   * @throws Exception the exception
   */
  @Test
  void calculateTax_withNegativeAmount_shouldReturnBadRequest() throws Exception {
    TaxInDTO invalidDTO = new TaxInDTO("TAX123456789", new BigDecimal("-50.00"));

    mockMvc
        .perform(
            post("/tax-calculation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
        .andExpect(status().isBadRequest());
  }

  /**
   * Calculate batch tax with valid input should return ok.
   *
   * @throws Exception the exception
   */
  @Test
  void calculateBatchTax_withValidInput_shouldReturnOk() throws Exception {
    // When & Then
    mockMvc
        .perform(
            post("/tax-calculation/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBatchInDTO)))
        .andExpect(status().isAccepted());
  }

  /**
   * Calculate batch tax with large batch should return ok.
   *
   * @throws Exception the exception
   */
  @Test
  void calculateBatchTax_withLargeBatch_shouldReturnOk() throws Exception {
    // Given
    List<TaxInDTO> taxBatch = new ArrayList<>();
    List<BigDecimal> calculatedTaxes = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      taxBatch.add(new TaxInDTO("TAX" + i, new BigDecimal("100.00")));
      calculatedTaxes.add(new BigDecimal("20.00"));
    }

    TaxBatchInDTO largeBatchDTO = new TaxBatchInDTO(taxBatch);
    //    when(taxService.calculateTaxBatch(any(TaxBatchInDTO.class))).thenReturn(calculatedTaxes);

    // When & Then
    mockMvc
        .perform(
            post("/tax-calculation/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeBatchDTO)))
        .andExpect(status().isAccepted());

    //    verify(taxService).calculateTaxBatch(largeBatchDTO);
  }

  /**
   * Calculate batch manual with valid input should return ok.
   *
   * @throws Exception the exception
   */
  @Test
  void calculateBatchManual_withValidInput_shouldReturnOk() throws Exception {
    // When & Then
    mockMvc
        .perform(
            post("/tax-calculation/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBatchInDTO)))
        .andExpect(status().isAccepted());
  }
}
