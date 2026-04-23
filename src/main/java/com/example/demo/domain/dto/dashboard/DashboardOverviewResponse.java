package com.example.demo.domain.dto.dashboard;

import java.math.BigDecimal;

public record DashboardOverviewResponse(
        BigDecimal todaySalesAmount,
        Long todaySalesOrders,
        BigDecimal todayPurchaseAmount,
        Long todayPurchaseOrders,
        Long lowStockCount
) {
}
