package com.example.demo.domain.dto.purchase;

import java.util.List;

public record PurchaseOrderPageResponse(
        List<PurchaseOrderSummaryResponse> records,
        long total
) {
}
