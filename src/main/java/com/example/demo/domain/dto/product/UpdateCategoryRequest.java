package com.example.demo.domain.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCategoryRequest(
        @NotBlank(message = "name is required") String name,
        @NotNull(message = "status is required") Integer status
) {
}
