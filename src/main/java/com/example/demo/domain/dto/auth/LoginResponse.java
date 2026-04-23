package com.example.demo.domain.dto.auth;

import java.util.List;

//登录响应DTO
public record LoginResponse(String token, String tokenType, String username, String role, List<String> permissions) {
}
