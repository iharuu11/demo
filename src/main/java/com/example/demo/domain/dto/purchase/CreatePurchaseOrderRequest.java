package com.example.demo.domain.dto.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreatePurchaseOrderRequest(
        // 供应商 ID：必填
        @NotNull(message = "supplierId is required") Long supplierId,
        // 采购明细：至少 1 条
        @NotEmpty(message = "items must not be empty") List<@Valid CreatePurchaseItemRequest> items
) {
}
