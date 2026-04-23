package com.example.demo.domain.dto.auth;

import java.util.List;

public record CurrentUserPermissionsResponse(
        String username,
        String role,
        //用户权限列表
        List<String> permissions
) {
}
