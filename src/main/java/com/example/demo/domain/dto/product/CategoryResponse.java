package com.example.demo.domain.dto.product;

public record CategoryResponse(
        Long id,
        String name,
        Long parentId,
        Integer sort,
        Integer status
) {
}
