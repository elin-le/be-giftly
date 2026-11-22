package elin.le.giftly.product.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.product.dto.ProductOptionResponse;
import elin.le.giftly.product.dto.ProductResponse;
import elin.le.giftly.product.service.ProductOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Options", description = "Product option APIs for displaying product variants and selections")
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    @GetMapping("/{productId}/options")
    @Operation(summary = "Get product options", description = "Retrieve all options (e.g., color, size) for a product")
    public ResponseEntity<ApiResponse<List<ProductOptionResponse>>> getProductOptions(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        List<ProductOptionResponse> options = productOptionService.getProductOptions(productId);
        return ResponseEntity.ok(ApiResponse.success("Product options retrieved successfully", options));
    }

    @GetMapping("/{productId}/resolve-variant")
    @Operation(summary = "Resolve variant by option values", description = "Find the product variant that matches the given option value IDs (e.g., color + size)")
    public ResponseEntity<ApiResponse<ProductResponse.VariantResponse>> resolveVariant(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Comma-separated option value IDs", required = true)
            @RequestParam List<UUID> optionValueIds) {
        ProductResponse.VariantResponse variant = productOptionService.resolveVariant(productId, optionValueIds);
        return ResponseEntity.ok(ApiResponse.success("Variant resolved successfully", variant));
    }
}
