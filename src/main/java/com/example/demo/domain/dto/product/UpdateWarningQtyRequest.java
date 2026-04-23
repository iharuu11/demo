package com.example.demo.domain.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateWarningQtyRequest(
        // 预警值：必须传，且不能小于 0
        // 业务含义：当库存 quantity <= warningQty 时，可以在看板/列表中提示“库存预警”
        @NotNull(message = "warningQty is required")
        @Min(value = 0, message = "warningQty must be >= 0")
        Integer warningQty
) {
}
