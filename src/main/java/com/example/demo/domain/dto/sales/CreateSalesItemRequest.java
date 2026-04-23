package com.example.demo.domain.dto.sales;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateSalesItemRequest(
        // 商品 ID
        @NotNull(message = "productId is required") Long productId,
        // 购买数量：至少 1
        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be >= 1") Integer quantity
) {
}
