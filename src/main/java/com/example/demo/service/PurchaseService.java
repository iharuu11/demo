package com.example.demo.service;

import com.example.demo.domain.dto.purchase.CreatePurchaseItemRequest;
import com.example.demo.domain.dto.purchase.CreatePurchaseOrderRequest;
import com.example.demo.domain.dto.purchase.CreateSupplierRequest;
import com.example.demo.domain.dto.purchase.PurchaseOrderDetailResponse;
import com.example.demo.domain.dto.purchase.PurchaseOrderSummaryResponse;
import com.example.demo.domain.dto.purchase.SupplierSummaryResponse;
import com.example.demo.domain.dto.purchase.UpdateSupplierRequest;
import com.example.demo.domain.dto.purchase.UpdateSupplierStatusRequest;
import com.example.demo.domain.entity.Inventory;
import com.example.demo.domain.entity.Product;
import com.example.demo.domain.entity.PurchaseOrder;
import com.example.demo.domain.entity.PurchaseOrderItem;
import com.example.demo.domain.entity.Supplier;
import com.example.demo.mapper.InventoryMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.mapper.PurchaseOrderMapper;
import com.example.demo.mapper.SupplierMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {
    private final SupplierMapper supplierMapper;
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;

    public PurchaseService(SupplierMapper supplierMapper,
                           ProductMapper productMapper,
                           InventoryMapper inventoryMapper,
                           PurchaseOrderMapper purchaseOrderMapper) {
        this.supplierMapper = supplierMapper;
        this.productMapper = productMapper;
        this.inventoryMapper = inventoryMapper;
        this.purchaseOrderMapper = purchaseOrderMapper;
    }

    @Transactional
    public Long createSupplier(CreateSupplierRequest request) {
        // 创建供应商（事务）
        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setContactName(request.contactName());
        supplier.setContactPhone(request.contactPhone());
        supplier.setAddress(request.address());
        supplier.setStatus(1);
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

    public List<Supplier> listSuppliers() {
        // 查询全部供应商（无分页）
        return supplierMapper.listAll();
    }

    public List<SupplierSummaryResponse> listSuppliers(String keyword, int pageNum, int pageSize) {
        // 分页查询供应商（支持关键字）
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return supplierMapper.listPaged(safeKeyword, safePageSize, offset).stream()
                .map(s -> new SupplierSummaryResponse(
                        s.getId(), s.getName(), s.getContactName(), s.getContactPhone(), s.getAddress(), s.getStatus(), s.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void updateSupplier(Long id, UpdateSupplierRequest request) {
        // 更新供应商信息：先校验存在，再更新
        Supplier supplier = supplierMapper.findById(id);
        if (supplier == null) {
            throw new IllegalArgumentException("supplier not found");
        }
        supplier.setName(request.name());
        supplier.setContactName(request.contactName());
        supplier.setContactPhone(request.contactPhone());
        supplier.setAddress(request.address());
        supplierMapper.update(supplier);
    }

    @Transactional
    public void updateSupplierStatus(Long id, UpdateSupplierStatusRequest request) {
        // 更新供应商状态（启用/停用）
        Supplier supplier = supplierMapper.findById(id);
        if (supplier == null) {
            throw new IllegalArgumentException("supplier not found");
        }
        supplier.setStatus(request.status());
        supplierMapper.updateStatus(supplier);
    }

    @Transactional
    public Long createOrder(CreatePurchaseOrderRequest request, String operator) {
        // 创建采购单（事务）：
        // 1) 校验供应商、商品存在
        // 2) 计算总金额
        // 3) 写采购主单 + 采购明细（初始状态 0: 待入库）

        //查询供应商是否存在
        Supplier supplier = supplierMapper.findById(request.supplierId());
        if (supplier == null) {
            throw new IllegalArgumentException("supplier not found");
        }
        //计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreatePurchaseItemRequest item : request.items()) {
            Product product = productMapper.findById(item.productId());
            if (product == null) {
                throw new IllegalArgumentException("product not found: " + item.productId());
            }
            totalAmount = totalAmount.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setSupplierId(request.supplierId());
        order.setStatus(0); //初始状态为0，待入库
        order.setTotalAmount(totalAmount);
        order.setCreatedBy(operator);
        //插入采购主单
        purchaseOrderMapper.insertOrder(order);

        //插入采购明细
        for (CreatePurchaseItemRequest item : request.items()) {
            PurchaseOrderItem poItem = new PurchaseOrderItem();
            poItem.setOrderId(order.getId());
            poItem.setProductId(item.productId());
            poItem.setQuantity(item.quantity());
            poItem.setUnitPrice(item.unitPrice());
            poItem.setAmount(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
            purchaseOrderMapper.insertItem(poItem);
        }
        return order.getId();
    }

    @Transactional
    public void stockIn(Long orderId, String operator) {
        // 采购入库（事务）：
        // - 仅待入库状态可执行（status=0）
        // - 明细商品逐个加库存，并写库存流水
        // - 最后把采购单状态改为已入库（status=1）
        PurchaseOrder order = purchaseOrderMapper.findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("purchase order not found");
        }
        if (order.getStatus() != 0) {
            throw new IllegalArgumentException("purchase order already stocked");
        }

        List<PurchaseOrderItem> items = purchaseOrderMapper.listItemsByOrderId(orderId);
        //逐个加库存，并写库存流水
        for (PurchaseOrderItem item : items) {
            Inventory inventory = inventoryMapper.findByProductId(item.getProductId());
            if (inventory == null) {
                Inventory newInventory = new Inventory();
                newInventory.setProductId(item.getProductId());
                newInventory.setQuantity(0);
                newInventory.setWarningQty(10);
                inventoryMapper.insert(newInventory);
                inventory = inventoryMapper.findByProductId(item.getProductId());
            }
            int afterQty = inventory.getQuantity() + item.getQuantity();
            inventoryMapper.addQuantity(item.getProductId(), item.getQuantity());
            inventoryMapper.insertLog(
                    item.getProductId(),
                    item.getQuantity(),
                    afterQty,
                    "PURCHASE_STOCK_IN",
                    "purchase order: " + order.getOrderNo(),
                    operator);
        }
        int updated = purchaseOrderMapper.updateStatus(orderId, 0, 1);
        if (updated == 0) {
            throw new IllegalArgumentException("purchase order status update failed");
        }
    }

    public List<PurchaseOrderSummaryResponse> listOrders(
            Integer status, String orderNo, String startTime, String endTime, int pageNum, int pageSize) {
        // 分页查询采购单（状态/单号/时间区间筛选）
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        String safeOrderNo = (orderNo == null || orderNo.isBlank()) ? null : orderNo.trim();
        String safeStartTime = (startTime == null || startTime.isBlank()) ? null : startTime.trim();
        String safeEndTime = (endTime == null || endTime.isBlank()) ? null : endTime.trim();
        return purchaseOrderMapper.listOrders(status, safeOrderNo, safeStartTime, safeEndTime, safePageSize, offset);
    }

    public PurchaseOrderDetailResponse orderDetail(Long id) {
        // 采购单详情（主单 + 明细 + 供应商名称）
        PurchaseOrder order = purchaseOrderMapper.findOrderById(id);
        if (order == null) {
            throw new IllegalArgumentException("purchase order not found");
        }
        Supplier supplier = supplierMapper.findById(order.getSupplierId());
        String supplierName = supplier == null ? null : supplier.getName();
        return new PurchaseOrderDetailResponse(
                order.getId(),
                order.getOrderNo(),
                order.getSupplierId(),
                supplierName,
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedBy(),
                order.getCreatedAt(),
                order.getAuditedAt(),
                purchaseOrderMapper.listOrderItemResponses(id)
        );
    }

    @Transactional
    public void cancelOrder(Long id) {
        // 取消采购单：通常只有待入库状态可取消
        PurchaseOrder order = purchaseOrderMapper.findOrderById(id);
        if (order == null) {
            throw new IllegalArgumentException("purchase order not found");
        }
        int updated = purchaseOrderMapper.cancelOrder(id);
        if (updated == 0) {
            throw new IllegalArgumentException("only pending purchase order can be cancelled");
        }
    }

    private String generateOrderNo() {
        // 采购单号示例：PO20260410123456
        return "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
