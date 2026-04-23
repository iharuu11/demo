package com.example.demo.domain.dto.purchase;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
        // 商品 ID
        Long productId,
        // 商品名称
        String productName,
        // 采购数量
        Integer quantity,
        // 采购单价
        BigDecimal unitPrice,
        // 本行金额 = 单价 * 数量
        BigDecimal amount
) {
}
