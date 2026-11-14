package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.CreateProductRequest;
import elin.le.giftly.admin.dto.UpdateProductRequest;
import elin.le.giftly.admin.service.AdminProductService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.product.entity.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Products", description = "Admin APIs for product CRUD management. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products with optional filtering by category, status, or type with pagination")
    public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by product status")
            @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "Filter by product type")
            @RequestParam(required = false) ProductType type,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Product> products;
        if (categoryId != null) {
            products = adminProductService.getProductsByCategory(categoryId, pageable);
        } else if (status != null) {
            products = adminProductService.getProductsByStatus(status, pageable);
        } else if (type != null) {
            products = adminProductService.getProductsByType(type, pageable);
        } else {
            products = adminProductService.getAllProducts(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<ApiResponse<Product>> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        Product product = adminProductService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product (component or gift box)")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @Parameter(description = "Product creation data", required = true)
            @Valid @RequestBody CreateProductRequest request) {
        Product product = adminProductService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product created successfully", product));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product", description = "Update an existing product's details")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Updated product data", required = true)
            @Valid @RequestBody UpdateProductRequest request) {
        Product product = adminProductService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product", description = "Delete a product by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID to delete", required = true)
            @PathVariable UUID productId) {
        adminProductService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
