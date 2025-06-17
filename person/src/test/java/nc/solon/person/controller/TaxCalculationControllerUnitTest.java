package nc.solon.person.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.event.TaxCalculationEvent;
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

@ExtendWith(MockitoExtension.class)
class TaxCalculationControllerUnitTest {

  @Mock private TaxService taxService;

  @InjectMocks private TaxCalculationController controller;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private TaxInDTO taxInDTO;
  private List<TaxInDTO> taxBatch;
  private ManualConsumeTaxOutDTO manualConsumeOutDTO;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    objectMapper = new ObjectMapper();

    // Create test data
    taxInDTO = new TaxInDTO();
    taxInDTO.setTaxId("TAX-123");
    taxInDTO.setAmount(BigDecimal.valueOf(1000.00));

    TaxInDTO taxInDTO2 = new TaxInDTO();
    taxInDTO2.setTaxId("TAX-456");
    taxInDTO2.setAmount(BigDecimal.valueOf(2000.00));

    taxBatch = Arrays.asList(taxInDTO, taxInDTO2);

    List<TaxCalculationEvent> events =
        Collections.singletonList(new TaxCalculationEvent("TAX-123", BigDecimal.valueOf(1000.00)));
    manualConsumeOutDTO = new ManualConsumeTaxOutDTO(events, 5, true);
  }

  @Test
  void calculateTax_shouldCallServiceAndReturnAccepted() throws Exception {
    // When/Then
    mockMvc
        .perform(
            post("/api/v1/tax-calculation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxInDTO)))
        .andExpect(status().isAccepted());

    // Verify service was called with correct parameter
    verify(taxService, times(1)).calculateTax(any(TaxInDTO.class));
  }

  @Test
  void calculateTaxBatch_shouldCallServiceAndReturnAccepted() throws Exception {
    // When/Then
    mockMvc
        .perform(
            post("/api/v1/tax-calculation/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxBatch)))
        .andExpect(status().isAccepted());

    // Verify service was called with correct parameter
    verify(taxService, times(1)).calculateTaxBatch(anyList());
  }

  @Test
  void produceManual_shouldCallServiceAndReturnAccepted() throws Exception {
    // When/Then
    mockMvc
        .perform(
            post("/api/v1/tax-calculation/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxBatch)))
        .andExpect(status().isAccepted());

    // Verify service was called with correct parameter
    verify(taxService, times(1)).produceManual(anyList());
  }

  @Test
  void consumeManual_shouldCallServiceWithDefaultCountAndReturnContent() throws Exception {
    // Given
    when(taxService.consumeManual(10)).thenReturn(manualConsumeOutDTO);

    // When/Then
    mockMvc
        .perform(get("/api/v1/tax-calculation/manual"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.batch[0].taxId").value("TAX-123"))
        .andExpect(jsonPath("$.batch[0].amount").value(1000.00))
        .andExpect(jsonPath("$.numberMessageLeft").value(5))
        .andExpect(jsonPath("$.hasMessageLeft").value(true));

    // Verify service was called with correct parameter (default count = 10)
    verify(taxService, times(1)).consumeManual(10);
  }

  @Test
  void consumeManual_shouldCallServiceWithCustomCount() throws Exception {
    // Given
    when(taxService.consumeManual(5)).thenReturn(manualConsumeOutDTO);

    // When/Then
    mockMvc
        .perform(get("/api/v1/tax-calculation/manual").param("count", "5"))
        .andExpect(status().isOk());

    // Verify service was called with custom count
    verify(taxService, times(1)).consumeManual(5);
  }

  @Test
  void consumeManual_shouldHandleParseException() throws Exception {
    // When/Then - verify that invalid count parameter causes a 400 Bad Request
    mockMvc
        .perform(get("/api/v1/tax-calculation/manual").param("count", "invalid"))
        .andExpect(status().isBadRequest());

    // Verify service was never called
    verify(taxService, never()).consumeManual(anyInt());
  }
}
