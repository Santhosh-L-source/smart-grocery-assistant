package com.grocery.assistant.service;

import com.grocery.assistant.entity.enums.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TamilVoiceServiceTest {

    private TamilVoiceService tamilVoiceService;

    @BeforeEach
    void setUp() {
        tamilVoiceService = new TamilVoiceService();
    }

    @Test
    @DisplayName("Convert numbers to Tamil words")
    void testConvertNumberToTamil() {
        assertEquals("ஐம்பத்தைந்து", tamilVoiceService.convertNumberToTamil(55));
        assertEquals("முப்பத்தைந்து", tamilVoiceService.convertNumberToTamil(35));
        assertEquals("நாற்பத்தெட்டு", tamilVoiceService.convertNumberToTamil(48));
        assertEquals("நூற்று பத்து", tamilVoiceService.convertNumberToTamil(110));
        assertEquals("நூற்று இருபது", tamilVoiceService.convertNumberToTamil(120));
        assertEquals("நூற்று எண்பத்தைந்து", tamilVoiceService.convertNumberToTamil(185));
    }

    @Test
    @DisplayName("Format product price announcement for Rice ₹55/kg")
    void testProductPriceAnnouncementRice() {
        String announcement = tamilVoiceService.formatProductPriceAnnouncement(
                "அரிசி", "Rice", Unit.KG, new BigDecimal("55.00")
        );
        assertEquals("ஒரு கிலோ அரிசி ஐம்பத்தைந்து ரூபாய்.", announcement);
    }

    @Test
    @DisplayName("Format product price announcement for Soap ₹35/piece")
    void testProductPriceAnnouncementSoap() {
        String announcement = tamilVoiceService.formatProductPriceAnnouncement(
                "சோப்", "Soap", Unit.PIECE, new BigDecimal("35.00")
        );
        assertEquals("ஒரு சோப்பின் விலை முப்பத்தைந்து ரூபாய்.", announcement);
    }

    @Test
    @DisplayName("Format Bill Total announcement for ₹185")
    void testBillTotalAnnouncement() {
        String announcement = tamilVoiceService.formatBillTotalAnnouncement(new BigDecimal("185.00"));
        assertEquals("மொத்த தொகை நூற்று எண்பத்தைந்து ரூபாய்.", announcement);
    }
}
