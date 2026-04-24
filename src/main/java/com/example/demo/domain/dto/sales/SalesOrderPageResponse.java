package com.example.demo.domain.dto.sales;

import java.util.List;

public record SalesOrderPageResponse(
        List<SalesOrderSummaryResponse> records,
        long total
) {
}
