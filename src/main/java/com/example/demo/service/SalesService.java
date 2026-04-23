package com.example.demo.service;

import com.example.demo.domain.dto.sales.CreateSalesItemRequest;
import com.example.demo.domain.dto.sales.CreateSalesOrderRequest;
import com.example.demo.domain.dto.sales.HotProductResponse;
import com.example.demo.domain.dto.sales.RefundSalesOrderRequest;
import com.example.demo.domain.dto.sales.SalesRefundResponse;
import com.example.demo.domain.dto.sales.SalesOrderDetailResponse;
import com.example.demo.domain.dto.sales.SalesOrderSummaryResponse;
import com.example.demo.domain.dto.sales.SalesOverviewResponse;
import com.example.demo.domain.entity.Inventory;
import com.example.demo.domain.entity.Product;
import com.example.demo.domain.entity.SalesOrder;
import com.example.demo.domain.entity.SalesOrderItem;
import com.example.demo.domain.entity.SalesRefund;
import com.example.demo.mapper.InventoryMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.mapper.SalesOrderMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesService {
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final SalesOrderMapper salesOrderMapper;

    public SalesService(ProductMapper productMapper, InventoryMapper inventoryMapper, SalesOrderMapper salesOrderMapper) {
        this.productMapper = productMapper;
        this.inventoryMapper = inventoryMapper;
        this.salesOrderMapper = salesOrderMapper;
    }

    @Transactional
    public Long createOrder(CreateSalesOrderRequest request, String cashierName) {
        // 创建销售单（事务）：
        // 1) 先逐项校验商品存在、库存充足，并计算订单总金额
        // 2) 写 sales_order 主单
        // 3) 扣减库存 + 写库存流水 + 写 sales_order_item 明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateSalesItemRequest item : request.items()) {
            Product product = productMapper.findById(item.productId());
            if (product == null) {
                throw new IllegalArgumentException("product not found: " + item.productId());
            }
            Inventory inventory = inventoryMapper.findByProductId(item.productId());
            if (inventory == null || inventory.getQuantity() < item.quantity()) {
                throw new IllegalArgumentException("stock not enough for product: " + item.productId());
            }
            totalAmount = totalAmount.add(product.getSalePrice().multiply(BigDecimal.valueOf(item.quantity())));
        }

        SalesOrder order = new SalesOrder();
        order.setOrderNo(generateOrderNo());
        order.setMemberId(request.memberId());
        order.setTotalAmount(totalAmount);
        order.setPaidAmount(totalAmount);
        order.setRefundAmount(BigDecimal.ZERO);
        order.setStatus(1);
        order.setPayType(request.payType());
        order.setCashierName(cashierName);
        salesOrderMapper.insertOrder(order);

        for (CreateSalesItemRequest item : request.items()) {
            Product product = productMapper.findById(item.productId());
            Inventory inventory = inventoryMapper.findByProductId(item.productId());
            int afterQty = inventory.getQuantity() - item.quantity();
            //扣减库存
            inventoryMapper.addQuantity(item.productId(), -item.quantity());
            //记录库存流水（卖出
            inventoryMapper.insertLog(item.productId(), -item.quantity(), afterQty,
                    "SALES_OUT", "sales order: " + order.getOrderNo(), cashierName);

            //记录销售明细
            SalesOrderItem orderItem = new SalesOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.productId());
            orderItem.setQuantity(item.quantity());
            orderItem.setUnitPrice(product.getSalePrice());
            orderItem.setAmount(product.getSalePrice().multiply(BigDecimal.valueOf(item.quantity())));
            salesOrderMapper.insertItem(orderItem);
        }
        return order.getId();
    }

    public SalesOverviewResponse todayOverview() {
        // 今日统计：总销售额、订单数、客单价
        String start = LocalDate.now().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = LocalDate.now().atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        BigDecimal total = salesOrderMapper.sumSalesAmount(start, end);
        long orders = salesOrderMapper.countOrders(start, end);
        BigDecimal avg = orders == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
        return new SalesOverviewResponse(total, orders, avg);
    }

    public List<HotProductResponse> todayHotProducts(int topN) {
        // 热销榜单：限制 topN 在 1~20
        int safeTopN = Math.min(Math.max(topN, 1), 20);
        String start = LocalDate.now().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = LocalDate.now().atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return salesOrderMapper.topHotProducts(start, end, safeTopN);
    }

    public List<SalesOrderSummaryResponse> listOrders(int pageNum, int pageSize) {
        // 分页查询销售单列表
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        return salesOrderMapper.listOrders(safePageSize, offset);
    }

    public SalesOrderDetailResponse getOrderDetail(Long id) {
        // 查询销售单详情（主单 + 明细）
        SalesOrder order = salesOrderMapper.findOrderById(id);
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        return new SalesOrderDetailResponse(
                order.getId(),
                order.getOrderNo(),
                order.getMemberId(),
                order.getTotalAmount(),
                order.getPaidAmount(),
                order.getPayType(),
                order.getCashierName(),
                order.getCreatedAt(),
                salesOrderMapper.listItemResponsesByOrderId(id));
    }

    @Transactional
    public void refundOrder(Long id, RefundSalesOrderRequest request, String operator) {
        // 退款流程（事务）：
        // 1) 校验销售单存在且未退款
        // 2) 将明细商品数量加回库存，并记录库存流水
        // 3) 更新销售单退款金额/状态
        // 4) 写 sales_refund 退款记录
        SalesOrder order = salesOrderMapper.findOrderById(id);
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        if (order.getStatus() != null && order.getStatus() == 3) {
            throw new IllegalArgumentException("sales order already refunded");
        }
        List<SalesOrderItem> items = salesOrderMapper.listItemsByOrderId(id);
        for (SalesOrderItem item : items) {
            //在库存中查询item对应的商品
            Inventory inventory = inventoryMapper.findByProductId(item.getProductId());
            int currentQty = inventory == null ? 0 : inventory.getQuantity();
            int afterQty = currentQty + item.getQuantity();
            //给没有库存记录的商品补充一条inventory记录
            if (inventory == null) {
                Inventory newInventory = new Inventory();
                newInventory.setProductId(item.getProductId());
                newInventory.setQuantity(0);
                newInventory.setWarningQty(10);
                inventoryMapper.insert(newInventory);
            }
            //将退货的商品补充回库存
            inventoryMapper.addQuantity(item.getProductId(), item.getQuantity());
            //记录库存流水（退货
            inventoryMapper.insertLog(item.getProductId(), item.getQuantity(), afterQty,
                    "SALES_REFUND_IN", "sales refund: " + order.getOrderNo(), operator);
        }

        //记录退款明细
        salesOrderMapper.updateRefundInfo(id, order.getPaidAmount(), 3);
        SalesRefund refund = new SalesRefund();
        refund.setSalesOrderId(id);
        refund.setRefundNo(generateRefundNo());
        refund.setRefundAmount(order.getPaidAmount());
        refund.setOperatorName(operator);
        refund.setReason(request.reason());
        salesOrderMapper.insertRefund(refund);
    }

    public List<SalesRefundResponse> listRefunds(int pageNum, int pageSize) {
        // 分页查询退款记录
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        return salesOrderMapper.listRefunds(safePageSize, offset);
    }

    //生成销售单号，私有方法只允许内部方法调用
    private String generateOrderNo() {
        // 订单号示例：SO20260410123456
        return "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
    
    //生成退款单号
    private String generateRefundNo() {
        // 退款单号示例：RF20260410123456
        return "RF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
