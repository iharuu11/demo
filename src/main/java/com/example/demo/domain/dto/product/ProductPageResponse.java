package com.example.demo.domain.dto.product;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> records,
        long total
) {
}
