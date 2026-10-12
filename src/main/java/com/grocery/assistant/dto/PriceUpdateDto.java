package com.grocery.assistant.dto;

import com.grocery.assistant.entity.enums.UpdateMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PriceUpdateDto {

    @NotNull(message = "New selling price is required")
    @DecimalMin(value = "0.01", message = "Selling price must be greater than zero")
    private BigDecimal newSellingPrice;

    @DecimalMin(value = "0.00", message = "Purchase price cannot be negative")
    private BigDecimal newPurchasePrice;

    private UpdateMethod updateMethod = UpdateMethod.MANUAL;

    private String changeReason;

    private Long expectedVersion;

    public PriceUpdateDto() {}

    public PriceUpdateDto(BigDecimal newSellingPrice, UpdateMethod updateMethod, String changeReason) {
        this.newSellingPrice = newSellingPrice;
        this.updateMethod = updateMethod;
        this.changeReason = changeReason;
    }

    public BigDecimal getNewSellingPrice() {
        return newSellingPrice;
    }

    public void setNewSellingPrice(BigDecimal newSellingPrice) {
        this.newSellingPrice = newSellingPrice;
    }

    public BigDecimal getNewPurchasePrice() {
        return newPurchasePrice;
    }

    public void setNewPurchasePrice(BigDecimal newPurchasePrice) {
        this.newPurchasePrice = newPurchasePrice;
    }

    public UpdateMethod getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(UpdateMethod updateMethod) {
        this.updateMethod = updateMethod;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(Long expectedVersion) {
        this.expectedVersion = expectedVersion;
    }
}
