package com.grocery.assistant.service;

import com.grocery.assistant.dto.SaleItemRequestDto;
import com.grocery.assistant.dto.SaleRequestDto;
import com.grocery.assistant.dto.SaleResponseDto;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.entity.Sale;
import com.grocery.assistant.entity.enums.ProductType;
import com.grocery.assistant.entity.enums.Unit;
import com.grocery.assistant.repository.ProductRepository;
import com.grocery.assistant.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Spy
    private UnitConversionService unitConversionService = new UnitConversionService();

    @Mock
    private TamilVoiceService tamilVoiceService;

    @InjectMocks
    private BillingService billingService;

    private Product rice;
    private Product soap;
    private Product sugar;

    @BeforeEach
    void setUp() {
        rice = new Product();
        rice.setId(1L);
        rice.setNameEn("Rice");
        rice.setNameTa("அரிசி");
        rice.setProductType(ProductType.LOOSE);
        rice.setUnit(Unit.KG);
        rice.setSellingPrice(new BigDecimal("55.00"));
        rice.setStockQuantity(new BigDecimal("100.000"));
        rice.setActive(true);

        soap = new Product();
        soap.setId(2L);
        soap.setNameEn("Soap");
        soap.setNameTa("சோப்");
        soap.setProductType(ProductType.PACKAGED);
        soap.setUnit(Unit.PIECE);
        soap.setSellingPrice(new BigDecimal("35.00"));
        soap.setStockQuantity(new BigDecimal("50.000"));
        soap.setActive(true);

        sugar = new Product();
        sugar.setId(3L);
        sugar.setNameEn("Sugar");
        sugar.setNameTa("சர்க்கரை");
        sugar.setProductType(ProductType.LOOSE);
        sugar.setUnit(Unit.KG);
        sugar.setSellingPrice(new BigDecimal("48.00"));
        sugar.setStockQuantity(new BigDecimal("80.000"));
        sugar.setActive(true);
    }

    @Test
    @DisplayName("Complete Sale: Rice (2kg x 55=110) + Soap (3 x 35=105) + Sugar (2.5kg x 48=120) = ₹335")
    void testCompleteSaleFullCalculation() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(rice));
        when(productRepository.findById(2L)).thenReturn(Optional.of(soap));
        when(productRepository.findById(3L)).thenReturn(Optional.of(sugar));

        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            s.setId(101L);
            return s;
        });

        when(tamilVoiceService.formatBillTotalAnnouncement(any())).thenReturn("மொத்த தொகை முந்நூற்று முப்பத்தைந்து ரூபாய்.");

        SaleRequestDto request = new SaleRequestDto();
        request.setItems(List.of(
                new SaleItemRequestDto(1L, new BigDecimal("2"), Unit.KG, new BigDecimal("55.00")),
                new SaleItemRequestDto(2L, new BigDecimal("3"), Unit.PIECE, new BigDecimal("35.00")),
                new SaleItemRequestDto(3L, new BigDecimal("2.5"), Unit.KG, new BigDecimal("48.00"))
        ));

        SaleResponseDto response = billingService.completeSale(request, "shopkeeper");

        assertNotNull(response);
        assertEquals(new BigDecimal("335.00"), response.getTotalAmount());
        assertEquals(new BigDecimal("335.00"), response.getSubtotal());
        assertEquals(3, response.getItems().size());
        assertEquals("மொத்த தொகை முந்நூற்று முப்பத்தைந்து ரூபாய்.", response.getSpokenTamilTotal());

        // Verify inventory reductions
        assertEquals(new BigDecimal("98.000"), rice.getStockQuantity());
        assertEquals(new BigDecimal("47.000"), soap.getStockQuantity());
        assertEquals(new BigDecimal("77.500"), sugar.getStockQuantity());
    }

    @Test
    @DisplayName("Duplicate sale submission returns existing sale idempotently")
    void testDuplicateSalePrevention() {
        Sale existingSale = new Sale("BILL-EXISTING-123", new BigDecimal("100.00"), BigDecimal.ZERO,
                new BigDecimal("100.00"), "CASH", "shopkeeper");
        existingSale.setId(99L);

        when(saleRepository.findBySaleNumber("BILL-EXISTING-123")).thenReturn(Optional.of(existingSale));

        SaleRequestDto request = new SaleRequestDto();
        request.setSaleNumber("BILL-EXISTING-123");
        request.setItems(List.of(new SaleItemRequestDto(1L, BigDecimal.ONE, Unit.KG, new BigDecimal("55.00"))));

        SaleResponseDto response = billingService.completeSale(request, "shopkeeper");

        assertNotNull(response);
        assertEquals("BILL-EXISTING-123", response.getSaleNumber());
        assertEquals(99L, response.getId());

        // Verify no extra save or product stock deduction occurred
        verify(saleRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Preserve historical sale price snapshot even after product price updates")
    void testPreservationOfHistoricalSalePrices() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(rice));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        SaleRequestDto request = new SaleRequestDto();
        request.setItems(List.of(
                new SaleItemRequestDto(1L, new BigDecimal("2"), Unit.KG, new BigDecimal("55.00"))
        ));

        SaleResponseDto response = billingService.completeSale(request, "shopkeeper");

        assertEquals(new BigDecimal("55.00"), response.getItems().get(0).getUnitPriceSnapshot());
        assertEquals(new BigDecimal("110.00"), response.getItems().get(0).getSubtotal());

        // Now suppose product rice price changes in database to ₹65/kg
        rice.setSellingPrice(new BigDecimal("65.00"));

        // The sale item snapshot remains strictly ₹55.00
        assertEquals(new BigDecimal("55.00"), response.getItems().get(0).getUnitPriceSnapshot());
        assertEquals(new BigDecimal("110.00"), response.getItems().get(0).getSubtotal());
    }
}
