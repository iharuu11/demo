package com.example.demo.domain.dto.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String barcode,
        Long categoryId,
        BigDecimal purchasePrice,
        BigDecimal salePrice,
        Integer status,
        Integer stockQty
) {
}
