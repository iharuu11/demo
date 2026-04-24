package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.domain.dto.sales.CreateSalesOrderRequest;
import com.example.demo.domain.dto.sales.HotProductResponse;
import com.example.demo.domain.dto.sales.RefundSalesOrderRequest;
import com.example.demo.domain.dto.sales.SalesRefundResponse;
import com.example.demo.domain.dto.sales.SalesOrderDetailResponse;
import com.example.demo.domain.dto.sales.SalesOrderPageResponse;
import com.example.demo.domain.dto.sales.SalesOrderSummaryResponse;
import com.example.demo.domain.dto.sales.SalesOverviewResponse;
import com.example.demo.service.SalesService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PreAuthorize("hasAuthority('sales:create')")
    @PostMapping("/orders")
    public ApiResponse<Long> createOrder(@Valid @RequestBody CreateSalesOrderRequest request, Authentication authentication) {
        // 创建销售单（需要 sales:create 权限）：
        // - 由当前登录用户作为收银员（cashier）
        // - 下单时会校验库存并扣减库存
        return ApiResponse.success(salesService.createOrder(request, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/stats/overview/today")
    public ApiResponse<SalesOverviewResponse> todayOverview() {
        // 今日销售概览：销售额、订单数、客单价
        return ApiResponse.success(salesService.todayOverview());
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/stats/hot-products/today")
    public ApiResponse<List<HotProductResponse>> todayHotProducts(@RequestParam(defaultValue = "10") int topN) {
        // 今日热销商品排行（topN）
        return ApiResponse.success(salesService.todayHotProducts(topN));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/orders")
    public ApiResponse<SalesOrderPageResponse> listOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 分页查询销售单列表
        return ApiResponse.success(salesService.listOrders(pageNum, pageSize));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/orders/{id}")
    public ApiResponse<SalesOrderDetailResponse> orderDetail(@PathVariable Long id) {
        // 查询销售单详情（含明细项）
        return ApiResponse.success(salesService.getOrderDetail(id));
    }

    @PreAuthorize("hasAuthority('sales:refund')")
    @PutMapping("/orders/{id}/refund")
    public ApiResponse<Void> refundOrder(
            @PathVariable Long id,
            @Valid @RequestBody RefundSalesOrderRequest request,
            Authentication authentication) {
        // 销售退款（需要 sales:refund 权限）：
        // - 已退款订单不可重复退款
        // - 退款会把商品数量加回库存，并记录库存流水与退款记录
        salesService.refundOrder(id, request, authentication.getName());
        return ApiResponse.success();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/refunds")
    public ApiResponse<List<SalesRefundResponse>> listRefunds(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 分页查询退款记录
        return ApiResponse.success(salesService.listRefunds(pageNum, pageSize));
    }
}
