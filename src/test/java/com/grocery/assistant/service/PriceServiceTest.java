package com.grocery.assistant.service;

import com.grocery.assistant.dto.PriceUpdateDto;
import com.grocery.assistant.dto.ProductDto;
import com.grocery.assistant.entity.PriceHistory;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.entity.enums.Unit;
import com.grocery.assistant.entity.enums.UpdateMethod;
import com.grocery.assistant.repository.PriceHistoryRepository;
import com.grocery.assistant.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private PriceService priceService;

    private Product rice;

    @BeforeEach
    void setUp() {
        rice = new Product();
        rice.setId(1L);
        rice.setNameEn("Rice");
        rice.setUnit(Unit.KG);
        rice.setPurchasePrice(new BigDecimal("45.00"));
        rice.setSellingPrice(new BigDecimal("50.00"));
        rice.setVersion(0L);
    }

    @Test
    @DisplayName("Manual price update records price_history and updates product")
    void testManualPriceUpdate() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(rice));
        when(productRepository.save(any(Product.class))).thenReturn(rice);
        when(productService.toDto(any(Product.class))).thenReturn(new ProductDto());

        PriceUpdateDto dto = new PriceUpdateDto();
        dto.setNewSellingPrice(new BigDecimal("55.00"));
        dto.setNewPurchasePrice(new BigDecimal("48.00"));
        dto.setUpdateMethod(UpdateMethod.MANUAL);
        dto.setChangeReason("Wholesale price increase");
        dto.setExpectedVersion(0L);

        priceService.updateProductPrice(1L, dto, "admin");

        // Verify product was saved with new price
        assertEquals(new BigDecimal("55.00"), rice.getSellingPrice());
        assertEquals(new BigDecimal("48.00"), rice.getPurchasePrice());

        // Verify PriceHistory was saved with old and new prices
        ArgumentCaptor<PriceHistory> historyCaptor = ArgumentCaptor.forClass(PriceHistory.class);
        verify(priceHistoryRepository).save(historyCaptor.capture());

        PriceHistory recorded = historyCaptor.getValue();
        assertEquals(new BigDecimal("50.00"), recorded.getOldSellingPrice());
        assertEquals(new BigDecimal("55.00"), recorded.getNewSellingPrice());
        assertEquals(new BigDecimal("45.00"), recorded.getOldPurchasePrice());
        assertEquals(new BigDecimal("48.00"), recorded.getNewPurchasePrice());
        assertEquals(UpdateMethod.MANUAL, recorded.getUpdateMethod());
        assertEquals("admin", recorded.getAdminUsername());
        assertEquals("Wholesale price increase", recorded.getChangeReason());
    }

    @Test
    @DisplayName("Reject invalid negative selling price")
    void testRejectNegativeSellingPrice() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(rice));

        PriceUpdateDto dto = new PriceUpdateDto();
        dto.setNewSellingPrice(new BigDecimal("-10.00"));

        assertThrows(IllegalArgumentException.class, () ->
                priceService.updateProductPrice(1L, dto, "admin")
        );
        verify(productRepository, never()).save(any());
        verify(priceHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Detect optimistic locking conflict when versions do not match")
    void testOptimisticLockingConflict() {
        rice.setVersion(2L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(rice));

        PriceUpdateDto dto = new PriceUpdateDto();
        dto.setNewSellingPrice(new BigDecimal("55.00"));
        dto.setExpectedVersion(1L); // Stale version from client

        assertThrows(IllegalStateException.class, () ->
                priceService.updateProductPrice(1L, dto, "admin")
        );
        verify(productRepository, never()).save(any());
    }
}
