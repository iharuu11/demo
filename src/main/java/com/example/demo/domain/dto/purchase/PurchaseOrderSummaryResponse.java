package com.example.demo.domain.dto.purchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseOrderSummaryResponse(
        // 采购单 ID
        Long id,
        // 采购单号
        String orderNo,
        // 供应商 ID
        Long supplierId,
        // 供应商名称
        String supplierName,
        // 状态码：0=待入库，1=已入库，2=已取消
        Integer status,
        // 状态文本（便于前端直接显示）
        String statusText,
        // 采购总金额
        BigDecimal totalAmount,
        // 创建人
        String createdBy,
        // 创建时间
        LocalDateTime createdAt,
        // 审核/入库时间
        LocalDateTime auditedAt
) {
}
