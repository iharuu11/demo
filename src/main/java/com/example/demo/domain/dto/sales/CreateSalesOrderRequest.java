package com.example.demo.domain.dto.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateSalesOrderRequest(
        // 会员 ID（可空：散客下单时不绑定会员）
        Long memberId,
        // 支付方式：例如 CASH / WECHAT / ALIPAY（具体枚举由前后端约定）
        @NotBlank(message = "payType is required") String payType,
        // 销售明细：至少 1 条
        @NotEmpty(message = "items must not be empty") List<@Valid CreateSalesItemRequest> items
) {
}
