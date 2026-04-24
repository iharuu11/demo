package com.example.demo.domain.dto.product;

import java.util.List;

public record InventoryLogPageResponse(
        List<InventoryLogResponse> records,
        long total
) {
}
