package elin.le.giftly.product.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.product.dto.PaginatedProductsResponse;
import elin.le.giftly.product.service.ProductService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedProductsResponse>> getProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cursor,
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
