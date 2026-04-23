package com.example.demo.domain.dto.rbac;

import jakarta.validation.constraints.NotNull;

public record AssignUserRoleRequest(
        @NotNull(message = "roleId is required") Long roleId
) {
}
