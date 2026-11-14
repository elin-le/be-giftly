package elin.le.giftly.product.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.product.dto.PaginatedProductsResponse;
import elin.le.giftly.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product browsing and search APIs for customers")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products", description = "Browse products with optional filtering by category, price range, and keyword search. Supports cursor-based pagination.")
    public ResponseEntity<ApiResponse<PaginatedProductsResponse>> getProducts(
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Search keyword for product name or description")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Cursor for pagination")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Number of items per page (default 20)")
            @RequestParam(defaultValue = "20") @Min(1) int pageSize
    ) {
        PaginatedProductsResponse response = productService.getProducts(
                categoryId,
                minPrice,
                maxPrice,
                keyword,
                cursor,
                pageSize
        );
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", response));
    }
}
