package com.example.demo.domain.dto.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SalesOrderDetailResponse(
        // 销售单 ID
        Long id,
        // 销售单号
        String orderNo,
        // 会员 ID（可空，表示散客）
        Long memberId,
        // 订单总金额
        BigDecimal totalAmount,
        // 实付金额
        BigDecimal paidAmount,
        // 支付方式
        String payType,
        // 收银员姓名
        String cashierName,
        // 下单时间
        LocalDateTime createdAt,
        // 销售明细列表
        List<SalesOrderItemResponse> items
) {
}
