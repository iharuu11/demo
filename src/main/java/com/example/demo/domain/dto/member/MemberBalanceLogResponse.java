package com.example.demo.domain.dto.member;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberBalanceLogResponse(
        Long id,
        Long memberId,
        String memberPhone,
        String memberName,
        BigDecimal deltaAmount,
        BigDecimal afterBalance,
        String bizType,
        String remark,
        String operatorName,
        LocalDateTime createdAt
) {
}
