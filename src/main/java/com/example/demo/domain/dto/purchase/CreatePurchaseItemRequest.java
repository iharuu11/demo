package com.example.demo.domain.dto.purchase;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePurchaseItemRequest(
        // 商品 ID
        @NotNull(message = "productId is required") Long productId,
        // 采购数量：至少 1
        @NotNull(message = "quantity is required") @Min(value = 1, message = "quantity must be >= 1") Integer quantity,
        // 采购单价：必须大于 0
        @NotNull(message = "unitPrice is required") @DecimalMin(value = "0.0", inclusive = false) BigDecimal unitPrice
) {
}
