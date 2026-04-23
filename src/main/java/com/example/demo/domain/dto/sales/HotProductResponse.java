package com.example.demo.domain.dto.sales;

public record HotProductResponse(
        // 商品 ID
        Long productId,
        // 商品名称
        String productName,
        // 今日售出总数量
        Integer totalQuantity
) {
}
