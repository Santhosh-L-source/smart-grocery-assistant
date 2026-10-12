package com.grocery.assistant.dto;

import com.grocery.assistant.entity.enums.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SaleItemRequestDto {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private Unit unit;

    private BigDecimal clientPriceSnapshot;

    public SaleItemRequestDto() {}

    public SaleItemRequestDto(Long productId, BigDecimal quantity, Unit unit, BigDecimal clientPriceSnapshot) {
        this.productId = productId;
        this.quantity = quantity;
        this.unit = unit;
        this.clientPriceSnapshot = clientPriceSnapshot;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public BigDecimal getClientPriceSnapshot() {
        return clientPriceSnapshot;
    }

    public void setClientPriceSnapshot(BigDecimal clientPriceSnapshot) {
        this.clientPriceSnapshot = clientPriceSnapshot;
    }
}
