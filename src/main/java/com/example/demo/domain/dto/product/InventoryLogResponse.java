package com.example.demo.domain.dto.product;

import java.time.LocalDateTime;

public record InventoryLogResponse(
        Long id,
        Long productId,
        String productName,
        Integer deltaQty,
        Integer afterQty,
        String bizType,
        String remark,
        String operatorName,
        LocalDateTime createdAt
) {
}
