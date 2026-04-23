package com.example.demo.domain.dto.purchase;

import java.time.LocalDateTime;

public record SupplierSummaryResponse(
        // 供应商 ID
        Long id,
        // 供应商名称
        String name,
        // 联系人姓名
        String contactName,
        // 联系电话
        String contactPhone,
        // 联系地址
        String address,
        // 状态（例如 1=启用，0=停用）
        Integer status,
        // 创建时间
        LocalDateTime createdAt
) {
}
