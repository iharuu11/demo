package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.domain.dto.product.AdjustInventoryRequest;
import com.example.demo.domain.dto.product.CategoryResponse;
import com.example.demo.domain.dto.product.CreateCategoryRequest;
import com.example.demo.domain.dto.product.CreateProductRequest;
import com.example.demo.domain.dto.product.InventoryInfoResponse;
import com.example.demo.domain.dto.product.InventoryLogPageResponse;
import com.example.demo.domain.dto.product.InventoryLogResponse;
import com.example.demo.domain.dto.product.InventoryPageResponse;
import com.example.demo.domain.dto.product.ProductResponse;
import com.example.demo.domain.dto.product.ProductPageResponse;
import com.example.demo.domain.dto.product.UpdateCategoryRequest;
import com.example.demo.domain.dto.product.UpdateProductRequest;
import com.example.demo.domain.dto.product.UpdateProductStatusRequest;
import com.example.demo.domain.dto.product.UpdateWarningQtyRequest;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
//商品控制器，用于处理商品相关的请求，对商品分类，商品，库存进行CRUD
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //@preauthorize用于校验用户是否登录，hasauthority('product:category:create')表示用户必须登录，且具有product:category:create权限
    @PreAuthorize("hasAuthority('product:category:create')")
    @PostMapping("/categories")
    public ApiResponse<Long> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        // 新增商品分类（需要对应的权限点）：
        // - @Valid：触发请求参数校验（例如分类名不能为空）
        return ApiResponse.success(productService.createCategory(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> listCategories() {
        // 查询分类列表（登录即可）
        return ApiResponse.success(productService.listCategories());
    }

    @PreAuthorize("hasAuthority('product:category:update')")
    @PutMapping("/categories/{id}")
    //@PathVariable用于获取路径中的id参数
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        // 更新分类（需要权限点）
        productService.updateCategory(id, request);
        return ApiResponse.success();
    }

    @PreAuthorize("hasAuthority('product:category:delete')")
    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        // 删除分类（需要权限点）
        productService.deleteCategory(id);
        return ApiResponse.success();
    }

    @PreAuthorize("hasAuthority('product:create')")
    @PostMapping
    public ApiResponse<Long> createProduct(@Valid @RequestBody CreateProductRequest request) {
        // 新增商品（需要权限点）：
        // - 创建商品后会同时初始化一条库存记录（quantity=0, warningQty=10）
        return ApiResponse.success(productService.createProduct(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping
    public ApiResponse<ProductPageResponse> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 分页查询商品：
        // - keyword：按商品名/条码等搜索（具体在 mapper 里实现）
        return ApiResponse.success(productService.listProducts(keyword, categoryId, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('product:update')")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        // 更新商品基础信息（需要权限点）
        productService.updateProduct(id, request);
        return ApiResponse.success();
    }

    @PreAuthorize("hasAuthority('product:status:update')")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateProductStatus(@PathVariable Long id, @Valid @RequestBody UpdateProductStatusRequest request) {
        // 上架/下架（启用/禁用）商品（需要权限点），更新商品status字段
        productService.updateProductStatus(id, request);
        return ApiResponse.success();
    }

    @PreAuthorize("hasAuthority('product:delete')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        // 删除商品（需要权限点）：
        // - 同时删除库存表里的对应记录
        productService.deleteProduct(id);
        return ApiResponse.success();
    }

    @PreAuthorize("hasAuthority('inventory:adjust')")
    @PutMapping("/{id}/inventory/adjust")
    public ApiResponse<Void> adjustInventory(
            @PathVariable("id") Long productId,
            @Valid @RequestBody AdjustInventoryRequest request,
            Authentication authentication) {
        // 手动调整库存（需要 inventory:adjust 权限点）：
        // - deltaQty：正数=加库存，负数=减库存
        // - authentication.getName()：当前操作人（用于记录库存流水）
        productService.adjustInventory(productId, request, authentication.getName());
        return ApiResponse.success();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/inventory")
    public ApiResponse<InventoryPageResponse> listInventory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 库存列表（用于前端“库存管理”页展示）
        return ApiResponse.success(productService.listInventory(keyword, categoryId, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('inventory:warning:update')")
    @PutMapping("/{id}/inventory/warning-qty")
    public ApiResponse<Void> updateWarningQty(@PathVariable("id") Long productId, @Valid @RequestBody UpdateWarningQtyRequest request) {
        // 修改库存预警值（需要权限点）
        productService.updateWarningQty(productId, request);
        return ApiResponse.success();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/inventory/logs")
    //查询库存流水，productId为空时查询所有，否则查询指定商品的流水
    public ApiResponse<InventoryLogPageResponse> listInventoryLogs(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(productService.listInventoryLogs(productId, pageNum, pageSize));
    }
}
