package com.example.demo.domain.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdjustInventoryRequest(
        // 库存变更数量：
        // - 正数：增加库存
        // - 负数：减少库存
        @NotNull(message = "deltaQty is required") Integer deltaQty,
        // 业务类型（用于库存流水分类）：
        // 例如：MANUAL（手工调整）、PURCHASE（采购入库）、SALE（销售出库）等
        @NotBlank(message = "bizType is required") String bizType,
        // 备注：记录本次变更原因（可选）
        String remark
) {
}
