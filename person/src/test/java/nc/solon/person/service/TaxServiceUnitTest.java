package nc.solon.person.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nc.solon.person.dto.ManualConsumeTaxOutDTO;
import nc.solon.person.dto.TaxInDTO;
import nc.solon.person.event.TaxCalculationEvent;
import nc.solon.person.kafka.tax.calculation.TaxCalculationConsumer;
import nc.solon.person.kafka.tax.calculation.TaxCalculationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxServiceUnitTest {

  @Mock private TaxCalculationProducer taxProducer;

  @Mock private TaxCalculationConsumer taxConsumer;

  @InjectMocks private TaxService taxService;

  @Captor private ArgumentCaptor<TaxCalculationEvent> eventCaptor;

  @Captor private ArgumentCaptor<List<TaxCalculationEvent>> eventListCaptor;

  private TaxInDTO taxInDTO;
  private List<TaxInDTO> taxBatch;
  private ManualConsumeTaxOutDTO manualConsumeTaxOutDTO;

  @BeforeEach
  void setUp() {
    // Setup test data
    taxInDTO = new TaxInDTO();
    taxInDTO.setTaxId("TAX-123");
    taxInDTO.setAmount(BigDecimal.valueOf(1000.00));

    TaxInDTO taxInDTO2 = new TaxInDTO();
    taxInDTO2.setTaxId("TAX-456");
    taxInDTO2.setAmount(BigDecimal.valueOf(2000.00));

    taxBatch = Arrays.asList(taxInDTO, taxInDTO2);

    List<TaxCalculationEvent> events =
        Collections.singletonList(new TaxCalculationEvent("TAX-123", BigDecimal.valueOf(1000.00)));
    manualConsumeTaxOutDTO = new ManualConsumeTaxOutDTO(events, 5, true);
  }

  @Test
  void calculateTax_shouldSendEventToProducer() {
    // When
    taxService.calculateTax(taxInDTO);

    // Then
    verify(taxProducer).sendEvent(eventCaptor.capture());
    TaxCalculationEvent capturedEvent = eventCaptor.getValue();
    assertEquals(taxInDTO.getTaxId(), capturedEvent.getTaxId());
    assertEquals(taxInDTO.getAmount(), capturedEvent.getAmount());
  }

  @Test
  void calculateTaxBatch_shouldSendBatchEventsToProducer() {
    // When
    taxService.calculateTaxBatch(taxBatch);

    // Then
    verify(taxProducer).sendBatchEvent(eventListCaptor.capture());
    List<TaxCalculationEvent> capturedEvents = eventListCaptor.getValue();

    assertEquals(2, capturedEvents.size());
    assertEquals(taxBatch.get(0).getTaxId(), capturedEvents.get(0).getTaxId());
    assertEquals(taxBatch.get(0).getAmount(), capturedEvents.get(0).getAmount());
    assertEquals(taxBatch.get(1).getTaxId(), capturedEvents.get(1).getTaxId());
    assertEquals(taxBatch.get(1).getAmount(), capturedEvents.get(1).getAmount());
  }

  @Test
  void produceManual_shouldSendManualEventsToProducer() {
    // When
    taxService.produceManual(taxBatch);

    // Then
    verify(taxProducer, times(2)).sendManualEvent(eventCaptor.capture());
    List<TaxCalculationEvent> capturedEvents = eventCaptor.getAllValues();

    assertEquals(2, capturedEvents.size());
    assertEquals(taxBatch.get(0).getTaxId(), capturedEvents.get(0).getTaxId());
    assertEquals(taxBatch.get(0).getAmount(), capturedEvents.get(0).getAmount());
    assertEquals(taxBatch.get(1).getTaxId(), capturedEvents.get(1).getTaxId());
    assertEquals(taxBatch.get(1).getAmount(), capturedEvents.get(1).getAmount());
  }

  @Test
  void consumeManual_shouldReturnDTOFromConsumer() {
    // Given
    when(taxConsumer.consumeManual(5)).thenReturn(manualConsumeTaxOutDTO);

    // When
    ManualConsumeTaxOutDTO result = taxService.consumeManual(5);

    // Then
    verify(taxConsumer).consumeManual(5);
    assertSame(manualConsumeTaxOutDTO, result);
    assertEquals(1, result.getBatch().size());
    assertEquals("TAX-123", result.getBatch().get(0).getTaxId());
    assertEquals(BigDecimal.valueOf(1000.00), result.getBatch().get(0).getAmount());
    assertEquals(5, result.getNumberMessageLeft());
    assertTrue(result.isHasMessageLeft());
  }
}
