package com.example.demo.domain.dto.purchase;

import jakarta.validation.constraints.NotBlank;

public record UpdateSupplierRequest(
        @NotBlank(message = "name is required") String name,
        String contactName,
        String contactPhone,
        String address
) {
}
