package com.example.demo.domain.dto.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalesRefundResponse(
        // 退款记录 ID
        Long id,
        // 对应的销售单 ID
        Long salesOrderId,
        // 退款单号
        String refundNo,
        // 退款金额
        BigDecimal refundAmount,
        // 操作人（谁执行了退款）
        String operatorName,
        // 退款原因
        String reason,
        // 退款时间
        LocalDateTime createdAt
) {
}
