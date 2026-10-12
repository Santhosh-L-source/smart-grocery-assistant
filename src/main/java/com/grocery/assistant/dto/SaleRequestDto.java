package com.grocery.assistant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleRequestDto {

    private String saleNumber;

    @NotEmpty(message = "Sale must contain at least one item")
    @Valid
    private List<SaleItemRequestDto> items = new ArrayList<>();

    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    private BigDecimal discount = BigDecimal.ZERO;

    private String paymentMode = "CASH";

    public SaleRequestDto() {}

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public List<SaleItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequestDto> items) {
        this.items = items;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }
}
