package com.example.demo.domain.dto.product;

public record InventoryInfoResponse(
        // 商品 ID
        Long productId,
        // 商品名称（方便库存页直接展示）
        String productName,
        // 当前库存数量
        Integer quantity,
        // 预警值（库存低于/等于该值，可能需要补货）
        Integer warningQty
) {
}
