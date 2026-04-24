package com.example.demo.service;

import com.example.demo.domain.dto.product.AdjustInventoryRequest;
import com.example.demo.domain.dto.product.CategoryResponse;
import com.example.demo.domain.dto.product.CreateCategoryRequest;
import com.example.demo.domain.dto.product.CreateProductRequest;
import com.example.demo.domain.dto.product.InventoryInfoResponse;
import com.example.demo.domain.dto.product.InventoryLogPageResponse;
import com.example.demo.domain.dto.product.InventoryLogResponse;
import com.example.demo.domain.dto.product.ProductResponse;
import com.example.demo.domain.dto.product.UpdateCategoryRequest;
import com.example.demo.domain.dto.product.UpdateProductRequest;
import com.example.demo.domain.dto.product.UpdateProductStatusRequest;
import com.example.demo.domain.dto.product.UpdateWarningQtyRequest;
import com.example.demo.domain.entity.Category;
import com.example.demo.domain.entity.Inventory;
import com.example.demo.domain.entity.Product;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.InventoryMapper;
import com.example.demo.mapper.ProductMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;

    public ProductService(CategoryMapper categoryMapper, ProductMapper productMapper, InventoryMapper inventoryMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.inventoryMapper = inventoryMapper;
    }

    @Transactional
    public Long createCategory(CreateCategoryRequest request) {
        // 创建分类（写库，使用事务）：
        // parentId/sort/status 如果前端不传，就给默认值，避免数据库出现 null
        Category category = new Category();
        category.setName(request.name());
        category.setParentId(request.parentId() == null ? 0L : request.parentId());
        category.setSort(request.sort() == null ? 0 : request.sort());
        category.setStatus(1);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Transactional
    public Long createProduct(CreateProductRequest request) {
        // 创建商品（写库，使用事务）：
        // - 条码 barcode 不允许重复（作为商品的重要标识）
        // - 商品创建成功后，同时初始化库存记录（避免后续查库存时找不到 inventory）
        Product existed = productMapper.findByBarcode(request.barcode());
        if (existed != null) {
            throw new IllegalArgumentException("barcode already exists");
        }
        Product product = new Product();
        product.setName(request.name());
        product.setBarcode(request.barcode());
        product.setCategoryId(request.categoryId());
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        product.setStatus(1);
        productMapper.insert(product);

        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);
        // warningQty：默认预警值（库存低于/等于该值，可在看板中提示）
        inventory.setWarningQty(10);
        inventoryMapper.insert(inventory);
        return product.getId();
    }

    public List<ProductResponse> listProducts(String keyword, int pageNum, int pageSize) {
        // 分页参数做安全处理，防止一次查询过大
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        // keyword 为空/全空格就当作不筛选，trim()用于去除字符串两端的空格
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return productMapper.list(safeKeyword, safePageSize, offset).stream().map(p -> {
            // 商品列表要带上当前库存数量，方便前端直接展示
            Inventory inventory = inventoryMapper.findByProductId(p.getId());
            //如果inventory为null，则qty为0，否则为inventory的quantity
            int qty = inventory == null ? 0 : inventory.getQuantity();
            return new ProductResponse(
                    p.getId(), p.getName(), p.getBarcode(), p.getCategoryId(),
                    p.getPurchasePrice(), p.getSalePrice(), p.getStatus(), qty);
        }).toList();
    }

    public List<CategoryResponse> listCategories() {
        // 分类列表：Entity -> DTO
        return categoryMapper.listAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getParentId(), c.getSort(), c.getStatus()))
                .toList();
    }

    @Transactional
    public void updateCategory(Long id, UpdateCategoryRequest request) {
        // 更新分类前先校验是否存在
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new IllegalArgumentException("category not found");
        }
        category.setName(request.name());
        category.setParentId(request.parentId());
        category.setSort(request.sort());
        category.setStatus(request.status());
        categoryMapper.update(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        // 删除分类前先校验：
        // - 分类存在
        // - 该分类下没有商品（否则会导致“商品引用了不存在的分类”）
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new IllegalArgumentException("category not found");
        }
        if (productMapper.countByCategoryId(id) > 0) {
            throw new IllegalArgumentException("category has related products");
        }
        categoryMapper.deleteById(id);
    }

    @Transactional
    public void updateProduct(Long id, UpdateProductRequest request) {
        // 更新商品：
        // - 先校验商品存在
        // - 再校验条码不与其它商品重复
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("product not found");
        }
        Product existed = productMapper.findByBarcodeExcludeId(request.barcode(), id);
        if (existed != null) {
            throw new IllegalArgumentException("barcode already exists");
        }
        product.setName(request.name());
        product.setBarcode(request.barcode());
        product.setCategoryId(request.categoryId());
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        productMapper.update(product);
    }

    @Transactional
    public void updateProductStatus(Long id, UpdateProductStatusRequest request) {
        // 更新商品状态（例如上架/下架）
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("product not found");
        }
        productMapper.updateStatus(id, request.status());
    }

    @Transactional
    public void deleteProduct(Long id) {
        // 删除商品（写库，使用事务）：
        // - 删除商品表记录
        // - 删除库存表记录（保持数据一致）
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("product not found");
        }
        productMapper.deleteById(id);
        inventoryMapper.deleteByProductId(id);
    }

    @Transactional
    public void adjustInventory(Long productId, AdjustInventoryRequest request, String operatorName) {
        // 调整库存（写库，使用事务）：
        // - deltaQty 可以是正数/负数
        // - afterQty 不能小于 0（库存不足）
        // - 调整成功后写一条库存流水（inventory_log），方便追踪谁改了多少
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("product not found");
        }
        Inventory inventory = inventoryMapper.findByProductId(productId);
        if (inventory == null) {
            throw new IllegalArgumentException("inventory not found");
        }
        int afterQty = inventory.getQuantity() + request.deltaQty();
        if (afterQty < 0) {
            throw new IllegalArgumentException("stock not enough");
        }
        inventoryMapper.addQuantity(productId, request.deltaQty());
        inventoryMapper.insertLog(productId, request.deltaQty(), afterQty, request.bizType(), request.remark(), operatorName);
    }

    public List<InventoryInfoResponse> listInventory(String keyword, int pageNum, int pageSize) {
        // 库存列表分页查询（用于库存管理页）
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return inventoryMapper.listInventory(safeKeyword, safePageSize, offset);
    }

    public InventoryLogPageResponse listInventoryLogs(Long productId, int pageNum, int pageSize) {
        // 库存流水分页查询（可按商品过滤）
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;
        List<InventoryLogResponse> records = inventoryMapper.listLogs(productId, safePageSize, offset);
        long total = inventoryMapper.countLogs(productId);
        return new InventoryLogPageResponse(records, total);
    }

    @Transactional
    public void updateWarningQty(Long productId, UpdateWarningQtyRequest request) {
        // 修改预警值：只更新 inventory.warning_qty
        Inventory inventory = inventoryMapper.findByProductId(productId);
        if (inventory == null) {
            throw new IllegalArgumentException("inventory not found");
        }
        inventoryMapper.updateWarningQty(productId, request.warningQty());
    }
}
