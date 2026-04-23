package com.example.demo.domain.dto.sales;

import java.math.BigDecimal;

public record SalesOrderItemResponse(
        // 商品 ID
        Long productId,
        // 商品名称
        String productName,
        // 销售数量
        Integer quantity,
        // 销售单价
        BigDecimal unitPrice,
        // 本行金额 = 单价 * 数量
        BigDecimal amount
) {
}
