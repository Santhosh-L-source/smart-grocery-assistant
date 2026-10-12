package com.grocery.assistant.service;

import com.grocery.assistant.entity.enums.Unit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class UnitConversionService {

    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    /**
     * Converts a given quantity in itemUnit into the product's baseUnit.
     * E.g., 500 GRAMS for a KG product converts to 0.500 KG.
     */
    public BigDecimal convertQuantityToBaseUnit(BigDecimal quantity, Unit itemUnit, Unit baseUnit) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (itemUnit == null || itemUnit == baseUnit) {
            return quantity;
        }

        // Weight conversion
        if (baseUnit == Unit.KG && itemUnit == Unit.GRAM) {
            return quantity.divide(THOUSAND, 3, RoundingMode.HALF_UP);
        }
        if (baseUnit == Unit.GRAM && itemUnit == Unit.KG) {
            return quantity.multiply(THOUSAND).setScale(3, RoundingMode.HALF_UP);
        }

        // Volume conversion
        if (baseUnit == Unit.LITRE && itemUnit == Unit.ML) {
            return quantity.divide(THOUSAND, 3, RoundingMode.HALF_UP);
        }
        if (baseUnit == Unit.ML && itemUnit == Unit.LITRE) {
            return quantity.multiply(THOUSAND).setScale(3, RoundingMode.HALF_UP);
        }

        throw new IllegalArgumentException(String.format("Incompatible unit conversion from %s to %s",
                itemUnit, baseUnit));
    }

    /**
     * Calculates the subtotal for a line item given product base unit price,
     * product base unit, item quantity, and item unit.
     */
    public BigDecimal calculateItemSubtotal(BigDecimal unitPrice, Unit baseUnit, BigDecimal quantity, Unit itemUnit) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        BigDecimal normalizedQuantity = convertQuantityToBaseUnit(quantity, itemUnit != null ? itemUnit : baseUnit, baseUnit);
        return unitPrice.multiply(normalizedQuantity).setScale(2, RoundingMode.HALF_UP);
    }
}
