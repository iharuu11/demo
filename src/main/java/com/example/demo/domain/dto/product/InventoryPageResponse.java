package com.example.demo.domain.dto.product;

import java.util.List;

public record InventoryPageResponse(
        List<InventoryInfoResponse> records,
        long total
) {
}
