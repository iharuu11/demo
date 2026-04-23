package com.example.demo.domain.dto.rbac;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionRequest(
        @NotBlank(message = "code is required") String code,
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "type is required") String type
) {
}
