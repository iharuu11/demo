package com.example.demo.domain.dto.member;

import java.util.List;

public record MemberBalanceLogPageResponse(
        List<MemberBalanceLogResponse> records,
        long total
) {
}
