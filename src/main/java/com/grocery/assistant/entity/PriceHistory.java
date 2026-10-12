package com.grocery.assistant.entity;

import com.grocery.assistant.entity.enums.UpdateMethod;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history", indexes = {
    @Index(name = "idx_price_hist_product_id", columnList = "product_id"),
    @Index(name = "idx_price_hist_created_at", columnList = "created_at")
})
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "old_selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal oldSellingPrice;

    @Column(name = "new_selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal newSellingPrice;

    @Column(name = "old_purchase_price", precision = 10, scale = 2)
    private BigDecimal oldPurchasePrice;

    @Column(name = "new_purchase_price", precision = 10, scale = 2)
    private BigDecimal newPurchasePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "update_method", nullable = false, length = 20)
    private UpdateMethod updateMethod;

    @Column(name = "admin_username", nullable = false, length = 50)
    private String adminUsername;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PriceHistory() {}

    public PriceHistory(Product product, BigDecimal oldSellingPrice, BigDecimal newSellingPrice,
                        BigDecimal oldPurchasePrice, BigDecimal newPurchasePrice,
                        UpdateMethod updateMethod, String adminUsername, String changeReason) {
        this.product = product;
        this.oldSellingPrice = oldSellingPrice;
        this.newSellingPrice = newSellingPrice;
        this.oldPurchasePrice = oldPurchasePrice;
        this.newPurchasePrice = newPurchasePrice;
        this.updateMethod = updateMethod;
        this.adminUsername = adminUsername;
        this.changeReason = changeReason;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
