package com.example.demo.service;

import com.example.demo.domain.dto.dashboard.DashboardOverviewResponse;
import com.example.demo.domain.dto.dashboard.InventoryWarningPageResponse;
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

    public InventoryWarningPageResponse inventoryWarnings(int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        List<InventoryWarningResponse> records = inventoryMapper.listWarnings(safePageSize, offset);
        long total = inventoryMapper.countWarnings();
        return new InventoryWarningPageResponse(records, total);
    }
}
