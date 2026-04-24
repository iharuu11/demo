package com.example.demo.domain.dto.dashboard;

import java.util.List;

public record InventoryWarningPageResponse(
        List<InventoryWarningResponse> records,
        long total
) {
}
