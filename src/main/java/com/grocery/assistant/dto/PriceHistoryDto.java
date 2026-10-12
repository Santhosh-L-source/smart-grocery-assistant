package com.grocery.assistant.dto;

import com.grocery.assistant.entity.enums.UpdateMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceHistoryDto {

    private Long id;
    private Long productId;
    private String productNameEn;
    private String productNameTa;
    private BigDecimal oldSellingPrice;
    private BigDecimal newSellingPrice;
    private BigDecimal oldPurchasePrice;
    private BigDecimal newPurchasePrice;
    private UpdateMethod updateMethod;
    private String adminUsername;
    private String changeReason;
    private LocalDateTime createdAt;

    public PriceHistoryDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductNameEn() {
        return productNameEn;
    }

    public void setProductNameEn(String productNameEn) {
        this.productNameEn = productNameEn;
    }

    public String getProductNameTa() {
        return productNameTa;
    }

    public void setProductNameTa(String productNameTa) {
        this.productNameTa = productNameTa;
    }

    public BigDecimal getOldSellingPrice() {
        return oldSellingPrice;
    }

    public void setOldSellingPrice(BigDecimal oldSellingPrice) {
        this.oldSellingPrice = oldSellingPrice;
    }

    public BigDecimal getNewSellingPrice() {
        return newSellingPrice;
    }

    public void setNewSellingPrice(BigDecimal newSellingPrice) {
        this.newSellingPrice = newSellingPrice;
    }

    public BigDecimal getOldPurchasePrice() {
        return oldPurchasePrice;
    }

    public void setOldPurchasePrice(BigDecimal oldPurchasePrice) {
        this.oldPurchasePrice = oldPurchasePrice;
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

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
