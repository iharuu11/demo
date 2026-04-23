package com.example.demo.domain.dto.dashboard;

public record InventoryWarningResponse(
        Long productId,
        String productName,
        Integer quantity,
        Integer warningQty
) {
}
