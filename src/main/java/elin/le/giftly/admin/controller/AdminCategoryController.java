package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.CreateCategoryRequest;
import elin.le.giftly.admin.dto.UpdateCategoryRequest;
import elin.le.giftly.admin.service.AdminCategoryService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.product.entity.Category;
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
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Categories", description = "Admin APIs for category CRUD management. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all categories with pagination")
    public ResponseEntity<ApiResponse<Page<Category>>> getAllCategories(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Category> categories = adminCategoryService.getAllCategories(pageable);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID categoryId) {
        Category category = adminCategoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PostMapping
    @Operation(summary = "Create category", description = "Create a new product category")
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @Parameter(description = "Category data with name and description", required = true)
            @Valid @RequestBody CreateCategoryRequest request) {
        Category category = adminCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Category created successfully", category));
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update category", description = "Update an existing category's details")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID categoryId,
            @Parameter(description = "Updated category data", required = true)
            @Valid @RequestBody UpdateCategoryRequest request) {
        Category category = adminCategoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category", description = "Delete a category by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID to delete", required = true)
            @PathVariable UUID categoryId) {
        adminCategoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
