package com.example.demo.domain.dto.sales;

import java.math.BigDecimal;

public record SalesOverviewResponse(
        // 今日总销售额
        BigDecimal totalSalesAmount,
        // 今日订单总数
        Long totalOrders,
        // 今日客单价 = 总销售额 / 订单数
        BigDecimal averageOrderAmount
) {
}
