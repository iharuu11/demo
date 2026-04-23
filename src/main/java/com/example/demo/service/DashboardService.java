package com.example.demo.service;

import com.example.demo.domain.dto.dashboard.DashboardOverviewResponse;
import com.example.demo.domain.dto.dashboard.InventoryWarningResponse;
import com.example.demo.mapper.InventoryMapper;
import com.example.demo.mapper.PurchaseOrderMapper;
import com.example.demo.mapper.SalesOrderMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final SalesOrderMapper salesOrderMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final InventoryMapper inventoryMapper;

    public DashboardService(SalesOrderMapper salesOrderMapper,
                            PurchaseOrderMapper purchaseOrderMapper,
                            InventoryMapper inventoryMapper) {
        this.salesOrderMapper = salesOrderMapper;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.inventoryMapper = inventoryMapper;
    }

    public DashboardOverviewResponse todayOverview() {
        String start = LocalDate.now().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = LocalDate.now().atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //查询今日销售总额
        BigDecimal salesAmount = salesOrderMapper.sumSalesAmount(start, end);
        //查询今日销售订单数
        long salesOrders = salesOrderMapper.countOrders(start, end);
        //查询今日采购总额
        BigDecimal purchaseAmount = purchaseOrderMapper.sumPurchaseAmount(start, end);
        //查询今日采购订单数
        long purchaseOrders = purchaseOrderMapper.countPurchaseOrders(start, end);
        //查询今日低库存数
        long lowStock = inventoryMapper.countWarnings();
        return new DashboardOverviewResponse(salesAmount, salesOrders, purchaseAmount, purchaseOrders, lowStock);
    }

    public List<InventoryWarningResponse> inventoryWarnings(int limit) {
        //限制limit在1到200之间
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        //查询库存警告列表
        return inventoryMapper.listWarnings(safeLimit);
    }
}
