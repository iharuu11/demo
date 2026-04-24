package com.example.demo.domain.dto.purchase;

import java.util.List;

public record SupplierPageResponse(
        List<SupplierSummaryResponse> records,
        long total
) {
}
