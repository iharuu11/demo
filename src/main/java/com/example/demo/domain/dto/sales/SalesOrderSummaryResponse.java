package com.example.demo.domain.dto.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalesOrderSummaryResponse(
        // 销售单 ID
        Long id,
        // 销售单号
        String orderNo,
        // 应收总金额
        BigDecimal totalAmount,
        // 实付金额
        BigDecimal paidAmount,
        // 支付方式
        String payType,
        // 收银员姓名
        String cashierName,
        // 下单时间
        LocalDateTime createdAt
) {
}
