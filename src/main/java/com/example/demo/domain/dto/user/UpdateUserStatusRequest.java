package com.example.demo.domain.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "status is required")
        @Min(value = 0, message = "status must be 0 or 1")
        @Max(value = 1, message = "status must be 0 or 1")
        Integer status
) {
}
