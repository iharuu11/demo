package com.example.demo.domain.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "barcode is required") String barcode,
        @NotNull(message = "categoryId is required") Long categoryId,
        @NotNull(message = "purchasePrice is required") @DecimalMin(value = "0.0", inclusive = false) BigDecimal purchasePrice,
        @NotNull(message = "salePrice is required") @DecimalMin(value = "0.0", inclusive = false) BigDecimal salePrice
) {
}
