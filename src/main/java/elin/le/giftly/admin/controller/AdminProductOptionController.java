package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.CreateProductOptionRequest;
import elin.le.giftly.admin.dto.CreateProductOptionValueRequest;
import elin.le.giftly.admin.dto.UpdateProductOptionRequest;
import elin.le.giftly.admin.service.AdminProductOptionService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.product.entity.ProductOption;
import elin.le.giftly.product.entity.ProductOptionValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin Product Options", description = "Admin APIs for managing product options (color, size, etc.)")
public class AdminProductOptionController {

    private final AdminProductOptionService adminProductOptionService;

    @PostMapping("/{productId}/options")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create product option", description = "Create a new option group (e.g., 'Màu sắc') for a product, optionally with values")
    public ResponseEntity<ApiResponse<ProductOption>> createOption(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Valid @RequestBody CreateProductOptionRequest request) {
        ProductOption option = adminProductOptionService.createOption(productId, request);
        return ResponseEntity.ok(ApiResponse.success("Option created successfully", option));
    }

    @PutMapping("/{productId}/options/{optionId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update product option", description = "Update an option group's name, display order, and/or replace all its values")
    public ResponseEntity<ApiResponse<ProductOption>> updateOption(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Option ID", required = true)
            @PathVariable UUID optionId,
            @Valid @RequestBody UpdateProductOptionRequest request) {
        ProductOption option = adminProductOptionService.updateOption(productId, optionId, request);
        return ResponseEntity.ok(ApiResponse.success("Option updated successfully", option));
    }

    @DeleteMapping("/{productId}/options/{optionId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete product option", description = "Delete an option group and all its values. Also removes variant-option links.")
    public ResponseEntity<ApiResponse<Void>> deleteOption(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Option ID to delete", required = true)
            @PathVariable UUID optionId) {
        adminProductOptionService.deleteOption(productId, optionId);
        return ResponseEntity.ok(ApiResponse.success("Option deleted successfully", null));
    }

    @PostMapping("/{productId}/options/{optionId}/values")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add option value", description = "Add a new value (e.g., 'Xanh lá') to an existing option group")
    public ResponseEntity<ApiResponse<ProductOptionValue>> addOptionValue(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Option ID", required = true)
            @PathVariable UUID optionId,
            @Valid @RequestBody CreateProductOptionValueRequest request) {
        ProductOptionValue value = adminProductOptionService.addOptionValue(productId, optionId, request);
        return ResponseEntity.ok(ApiResponse.success("Option value added successfully", value));
    }

    @PutMapping("/{productId}/options/{optionId}/values/{valueId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update option value", description = "Update a specific option value's name and/or color hex code")
    public ResponseEntity<ApiResponse<ProductOptionValue>> updateOptionValue(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Option ID", required = true)
            @PathVariable UUID optionId,
            @Parameter(description = "Option Value ID", required = true)
            @PathVariable UUID valueId,
            @Valid @RequestBody CreateProductOptionValueRequest request) {
        ProductOptionValue value = adminProductOptionService.updateOptionValue(productId, optionId, valueId, request);
        return ResponseEntity.ok(ApiResponse.success("Option value updated successfully", value));
    }

    @DeleteMapping("/{productId}/options/{optionId}/values/{valueId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete option value", description = "Delete a specific option value from an option group. Also removes variant-option links for this value.")
    public ResponseEntity<ApiResponse<Void>> deleteOptionValue(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Option ID", required = true)
            @PathVariable UUID optionId,
            @Parameter(description = "Option Value ID to delete", required = true)
            @PathVariable UUID valueId) {
        adminProductOptionService.deleteOptionValue(productId, optionId, valueId);
        return ResponseEntity.ok(ApiResponse.success("Option value deleted successfully", null));
    }
}
