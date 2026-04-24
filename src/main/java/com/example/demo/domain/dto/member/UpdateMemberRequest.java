package com.example.demo.domain.dto.member;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateMemberRequest(
        @NotBlank(message = "name is required")
        @Size(max = 32, message = "name length must be <= 32")
        String name,
        Integer gender,
        Integer level,
        Integer points,
        @DecimalMin(value = "0.0", inclusive = true, message = "balance must be >= 0")
        BigDecimal balance
) {
}
