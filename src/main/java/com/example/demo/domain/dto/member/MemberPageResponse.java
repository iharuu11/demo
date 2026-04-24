package com.example.demo.domain.dto.member;

import java.util.List;

public record MemberPageResponse(
        List<MemberResponse> records,
        long total
) {
}
