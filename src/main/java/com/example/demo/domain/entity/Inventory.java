package com.example.demo.domain.entity;

import java.time.LocalDateTime;

public class Inventory {
    private Long productId;
    private Integer quantity;
    private Integer warningQty;
    private LocalDateTime updatedAt;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getWarningQty() { return warningQty; }
    public void setWarningQty(Integer warningQty) { this.warningQty = warningQty; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
