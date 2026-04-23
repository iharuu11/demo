package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.domain.dto.rbac.AssignUserRoleRequest;
import com.example.demo.domain.dto.rbac.CreatePermissionRequest;
import com.example.demo.domain.dto.rbac.CreateRoleRequest;
import com.example.demo.service.RbacService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rbac")
@PreAuthorize("hasRole('ADMIN')")
//RBAC控制器，新建角色，权限，rbac模型，分配给角色权限，分配给用户角色
public class RbacController {
    private final RbacService rbacService;

    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping("/roles")
    //新建角色（admin,staff
    public ApiResponse<Long> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(rbacService.createRole(request.code(), request.name()));
    }

    @PostMapping("/permissions")
    //新建权限
    public ApiResponse<Long> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return ApiResponse.success(rbacService.createPermission(request.code(), request.name(), request.type()));
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    //分配角色的权限
    public ApiResponse<Void> assignPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        rbacService.assignPermissionToRole(roleId, permissionId);
        return ApiResponse.success();
    }

    @PostMapping("/users/{userId}/role")
    //分配给用户角色
    public ApiResponse<Void> assignUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignUserRoleRequest request) {
        rbacService.assignRoleToUser(userId, request.roleId());
        return ApiResponse.success();
    }
}
