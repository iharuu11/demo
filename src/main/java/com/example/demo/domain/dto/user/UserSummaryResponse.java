package com.example.demo.domain.dto.user;

import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String username,
        String role,
        Integer status,
        LocalDateTime createdAt
) {
}
