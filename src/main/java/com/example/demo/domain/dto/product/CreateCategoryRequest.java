package com.example.demo.domain.dto.product;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "name is required") String name,
        Long parentId,
        Integer sort
) {
}
