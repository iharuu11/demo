package com.example.demo.domain.dto.user;

import java.util.List;

public record UserPageResponse(List<UserSummaryResponse> records, long total) {
}
