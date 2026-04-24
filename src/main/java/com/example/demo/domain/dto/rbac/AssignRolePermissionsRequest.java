package com.example.demo.domain.dto.rbac;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AssignRolePermissionsRequest(
        @NotNull(message = "permissionIds is required") List<Long> permissionIds
) {
}
