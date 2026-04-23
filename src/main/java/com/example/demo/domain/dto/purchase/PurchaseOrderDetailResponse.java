package com.example.demo.domain.dto.purchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderDetailResponse(
        // 采购单 ID
        Long id,
        // 采购单号
        String orderNo,
        // 供应商 ID
        Long supplierId,
        // 供应商名称
        String supplierName,
        // 状态码
        Integer status,
        // 采购总金额
        BigDecimal totalAmount,
        // 创建人
        String createdBy,
        // 创建时间
        LocalDateTime createdAt,
        // 审核/入库时间
        LocalDateTime auditedAt,
        // 采购明细列表
        List<PurchaseOrderItemResponse> items
) {
}
