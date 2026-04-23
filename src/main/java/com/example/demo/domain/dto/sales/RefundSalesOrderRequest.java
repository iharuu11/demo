package com.example.demo.domain.dto.sales;

import jakarta.validation.constraints.NotBlank;

public record RefundSalesOrderRequest(
        // 退款原因：必填（用于审计/追踪）
        @NotBlank(message = "reason is required") String reason
) {
}
