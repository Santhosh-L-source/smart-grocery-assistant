package com.grocery.assistant.service;

import com.grocery.assistant.entity.enums.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UnitConversionServiceTest {

    private UnitConversionService unitConversionService;

    @BeforeEach
    void setUp() {
        unitConversionService = new UnitConversionService();
    }

    @Test
    @DisplayName("Grams to Kilograms conversion (500g = 0.500 kg)")
    void testGramsToKilograms() {
        BigDecimal result = unitConversionService.convertQuantityToBaseUnit(
                new BigDecimal("500"), Unit.GRAM, Unit.KG
        );
        assertEquals(new BigDecimal("0.500"), result);
    }

    @Test
    @DisplayName("Millilitres to Litres conversion (250ml = 0.250 L)")
    void testMlToLitres() {
        BigDecimal result = unitConversionService.convertQuantityToBaseUnit(
                new BigDecimal("250"), Unit.ML, Unit.LITRE
        );
        assertEquals(new BigDecimal("0.250"), result);
    }

    @Test
    @DisplayName("Incompatible unit conversion throws IllegalArgumentException")
    void testIncompatibleUnits() {
        assertThrows(IllegalArgumentException.class, () ->
                unitConversionService.convertQuantityToBaseUnit(new BigDecimal("2"), Unit.PIECE, Unit.KG)
        );
    }

    @Test
    @DisplayName("Mandatory Test Case 1: Rice 2 kg × ₹55/kg = ₹110")
    void testRiceCalculation() {
        BigDecimal subtotal = unitConversionService.calculateItemSubtotal(
                new BigDecimal("55.00"), Unit.KG, new BigDecimal("2"), Unit.KG
        );
        assertEquals(new BigDecimal("110.00"), subtotal);
    }

    @Test
    @DisplayName("Mandatory Test Case 2: Soap 3 pieces × ₹35/piece = ₹105")
    void testSoapCalculation() {
        BigDecimal subtotal = unitConversionService.calculateItemSubtotal(
                new BigDecimal("35.00"), Unit.PIECE, new BigDecimal("3"), Unit.PIECE
        );
        assertEquals(new BigDecimal("105.00"), subtotal);
    }

    @Test
    @DisplayName("Mandatory Test Case 3: Sugar 2.5 kg × ₹48/kg = ₹120")
    void testSugarCalculation() {
        BigDecimal subtotal = unitConversionService.calculateItemSubtotal(
                new BigDecimal("48.00"), Unit.KG, new BigDecimal("2.5"), Unit.KG
        );
        assertEquals(new BigDecimal("120.00"), subtotal);
    }

    @Test
    @DisplayName("Loose Loose grams calculation: 500g of sugar at ₹48/kg = ₹24")
    void testSugarGramsCalculation() {
        BigDecimal subtotal = unitConversionService.calculateItemSubtotal(
                new BigDecimal("48.00"), Unit.KG, new BigDecimal("500"), Unit.GRAM
        );
        assertEquals(new BigDecimal("24.00"), subtotal);
    }
}
