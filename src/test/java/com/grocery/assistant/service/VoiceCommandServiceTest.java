package com.grocery.assistant.service;

import com.grocery.assistant.dto.ProductDto;
import com.grocery.assistant.dto.VoicePriceParseResult;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.entity.enums.ProductType;
import com.grocery.assistant.entity.enums.Unit;
import com.grocery.assistant.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoiceCommandServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private TamilVoiceService tamilVoiceService;

    @InjectMocks
    private VoiceCommandService voiceCommandService;

    private Product rice;
    private Product oil1;
    private Product oil2;

    @BeforeEach
    void setUp() {
        rice = new Product();
        rice.setId(1L);
        rice.setNameEn("Rice (Ponni)");
        rice.setNameTa("பொன்னி அரிசி");
        rice.setUnit(Unit.KG);
        rice.setSellingPrice(new BigDecimal("50.00"));

        oil1 = new Product();
        oil1.setId(2L);
        oil1.setNameEn("Sunflower Oil");
        oil1.setUnit(Unit.LITRE);
        oil1.setSellingPrice(new BigDecimal("120.00"));

        oil2 = new Product();
        oil2.setId(3L);
        oil2.setNameEn("Gingelly Oil");
        oil2.setUnit(Unit.LITRE);
        oil2.setSellingPrice(new BigDecimal("210.00"));
    }

    @Test
    @DisplayName("Parse clear voice command: 'Change rice price to 55 rupees per kilogram'")
    void testParseClearVoiceCommand() {
        when(productRepository.searchActiveProducts(eq("rice"))).thenReturn(List.of(rice));
        when(tamilVoiceService.formatVoiceConfirmationPrompt(any(), any(), eq(new BigDecimal("55"))))
                .thenReturn("பொன்னி அரிசி விலை ஐம்பத்தைந்து ரூபாய் என்று மாற்றவா?");

        VoicePriceParseResult result = voiceCommandService.parseVoicePriceCommand("Change rice price to 55 rupees per kilogram");

        assertNotNull(result);
        assertTrue(result.isMatched());
        assertFalse(result.isAmbiguous());
        assertEquals(1L, result.getProductId());
        assertEquals(Unit.KG, result.getUnit());
        assertEquals(new BigDecimal("50.00"), result.getCurrentSellingPrice());
        assertEquals(new BigDecimal("55"), result.getProposedSellingPrice());
        assertEquals("பொன்னி அரிசி விலை ஐம்பத்தைந்து ரூபாய் என்று மாற்றவா?", result.getSpokenConfirmationTamil());

        // Verify NO database updates were performed
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ambiguous voice command with multiple matches returns candidates and does not update")
    void testAmbiguousVoiceCommand() {
        when(productRepository.searchActiveProducts(eq("oil"))).thenReturn(List.of(oil1, oil2));
        when(productService.toDto(any())).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            ProductDto dto = new ProductDto();
            dto.setId(p.getId());
            dto.setNameEn(p.getNameEn());
            dto.setSellingPrice(p.getSellingPrice());
            return dto;
        });

        VoicePriceParseResult result = voiceCommandService.parseVoicePriceCommand("Change oil price to 130 rupees");

        assertNotNull(result);
        assertFalse(result.isMatched());
        assertTrue(result.isAmbiguous());
        assertEquals(2, result.getMatchingCandidates().size());
        assertTrue(result.getErrorMessage().contains("Multiple products found"));

        // Verify zero database updates
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Command with unparseable price returns clear error")
    void testVoiceCommandWithoutPrice() {
        VoicePriceParseResult result = voiceCommandService.parseVoicePriceCommand("Change rice price quickly");

        assertNotNull(result);
        assertFalse(result.isMatched());
        assertTrue(result.getErrorMessage().contains("Could not identify the new price"));
    }
}
