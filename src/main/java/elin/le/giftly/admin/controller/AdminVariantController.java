package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.UpdateVariantRequest;
import elin.le.giftly.admin.service.AdminProductService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.product.entity.ProductVariant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/variants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Variants", description = "Admin APIs for individual variant management. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminVariantController {

    private final AdminProductService adminProductService;

    @PutMapping("/{variantId}")
    @Operation(summary = "Update variant", description = "Update a specific variant's details independently")
    public ResponseEntity<ApiResponse<ProductVariant>> updateVariant(
            @Parameter(description = "Variant ID", required = true)
            @PathVariable UUID variantId,
            @Parameter(description = "Updated variant data", required = true)
            @Valid @RequestBody UpdateVariantRequest request) {
        ProductVariant variant = adminProductService.updateVariant(variantId, request);
        return ResponseEntity.ok(ApiResponse.success("Variant updated successfully", variant));
    }
}
