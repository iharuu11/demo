package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.domain.dto.purchase.CreatePurchaseOrderRequest;
import com.example.demo.domain.dto.purchase.CreateSupplierRequest;
import com.example.demo.domain.dto.purchase.PurchaseOrderDetailResponse;
import com.example.demo.domain.dto.purchase.PurchaseOrderPageResponse;
import com.example.demo.domain.dto.purchase.PurchaseOrderSummaryResponse;
import com.example.demo.domain.dto.purchase.SupplierPageResponse;
import com.example.demo.domain.dto.purchase.SupplierSummaryResponse;
import com.example.demo.domain.dto.purchase.UpdateSupplierRequest;
import com.example.demo.domain.dto.purchase.UpdateSupplierStatusRequest;
import com.example.demo.domain.entity.Supplier;
import com.example.demo.service.PurchaseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchases")
//采购控制器，用于处理采购相关的请求，对供应商，采购单进行CRUD
public class PurchaseController {
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PreAuthorize("hasAuthority('purchase:supplier:create')")
    @PostMapping("/suppliers")
    public ApiResponse<Long> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        // 新增供应商（需要权限点）
        return ApiResponse.success(purchaseService.createSupplier(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/suppliers")
    public ApiResponse<List<Supplier>> listSuppliers() {
        // 查询供应商列表（无分页）
        return ApiResponse.success(purchaseService.listSuppliers());
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/suppliers/page")
    public ApiResponse<SupplierPageResponse> listSuppliersPaged(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 分页查询供应商（支持关键字）
        return ApiResponse.success(purchaseService.listSuppliers(keyword, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('purchase:supplier:update')")
    @PutMapping("/suppliers/{id}")
    public ApiResponse<Void> updateSupplier(@PathVariable Long id, @Valid @RequestBody UpdateSupplierRequest request) {
        // 更新供应商基本信息
        purchaseService.updateSupplier(id, request);
        return ApiResponse.success();
    }

    @PreAuthorize("hasAuthority('purchase:supplier:status:update')")
    @PutMapping("/suppliers/{id}/status")
    public ApiResponse<Void> updateSupplierStatus(@PathVariable Long id, @Valid @RequestBody UpdateSupplierStatusRequest request) {
        // 更新供应商状态（启用/停用）
        purchaseService.updateSupplierStatus(id, request);
        return ApiResponse.success();
    }

    @PreAuthorize("hasAuthority('purchase:order:create')")
    @PostMapping("/orders")
    public ApiResponse<Long> createOrder(@Valid @RequestBody CreatePurchaseOrderRequest request, Authentication authentication) {
        // 创建采购单（初始状态：待入库）
        return ApiResponse.success(purchaseService.createOrder(request, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/orders")
    public ApiResponse<PurchaseOrderPageResponse> listOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 分页查询采购单（支持状态、单号、时间区间筛选）
        return ApiResponse.success(purchaseService.listOrders(status, orderNo, startTime, endTime, pageNum, pageSize));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/orders/{id}")
    public ApiResponse<PurchaseOrderDetailResponse> orderDetail(@PathVariable Long id) {
        // 根据id查询采购单详情（含采购明细）
        return ApiResponse.success(purchaseService.orderDetail(id));
    }

    @PreAuthorize("hasAuthority('purchase:order:stock-in')")
    @PutMapping("/orders/{id}/stock-in")
    public ApiResponse<Void> stockIn(@PathVariable("id") Long orderId, Authentication authentication) {
        // 采购入库：
        // - 仅“待入库”状态可执行
        // - 执行后会增加库存并记录库存流水
        purchaseService.stockIn(orderId, authentication.getName());
        return ApiResponse.success();
    }

    @PreAuthorize("hasAuthority('purchase:order:cancel')")
    @PutMapping("/orders/{id}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable("id") Long orderId) {
        // 取消采购单（通常只有待入库状态可取消）
        purchaseService.cancelOrder(orderId);
        return ApiResponse.success();
    }
}
