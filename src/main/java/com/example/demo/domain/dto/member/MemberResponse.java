package com.example.demo.domain.dto.member;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberResponse(
        // 会员 ID（数据库主键）
        Long id,
        // 手机号（会员唯一标识之一）
        String phone,
        // 会员姓名
        String name,
        // 性别（具体取值由业务约定）
        Integer gender,
        // 会员等级（例如 1=普通会员）
        Integer level,
        // 积分（消费累计）
        Integer points,
        // 余额（会员储值）
        BigDecimal balance,
        // 状态（1=启用）
        Integer status,
        // 创建时间（注册/创建会员的时间）
        LocalDateTime createdAt
) {
}
