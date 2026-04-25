package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.domain.dto.dashboard.DashboardOverviewResponse;
import com.example.demo.domain.dto.dashboard.InventoryWarningPageResponse;
import com.example.demo.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    //@preauthorize用于校验用户是否登录，hasAuthority('dashboard:view')表示用户必须登录，且具有dashboard:view权限
    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping("/overview/today")
    //返回今日销售概览，销售额、订单数、客单价
    public ApiResponse<DashboardOverviewResponse> todayOverview() {
        return ApiResponse.success(dashboardService.todayOverview());
    }

    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping("/inventory-warnings")
    public ApiResponse<InventoryWarningPageResponse> inventoryWarnings(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(dashboardService.inventoryWarnings(pageNum, pageSize));
    }
}
