package elin.le.giftly.product.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.product.dto.*;
import elin.le.giftly.product.service.BundleOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bundles")
@RequiredArgsConstructor
@Tag(name = "Bundles", description = "Bundle option APIs for gift box configuration")
public class BundleOptionController {

    private final BundleOptionService bundleOptionService;

    @GetMapping("/variants/{variantId}/option-groups")
    @Operation(summary = "Get bundle option groups", description = "Retrieve all option groups for a bundle variant (e.g., greeting cards, packaging)")
    public ResponseEntity<ApiResponse<List<BundleOptionGroupResponse>>> getBundleOptionGroups(
            @Parameter(description = "Bundle variant ID", required = true)
            @PathVariable UUID variantId) {
        List<BundleOptionGroupResponse> groups = bundleOptionService.getBundleOptionGroups(variantId);
        return ResponseEntity.ok(ApiResponse.success("Bundle option groups retrieved successfully", groups));
    }

    @GetMapping("/variants/{variantId}/components")
    @Operation(summary = "Get bundle components", description = "Retrieve all fixed components for a bundle variant")
    public ResponseEntity<ApiResponse<List<BundleComponentResponse>>> getBundleComponents(
            @Parameter(description = "Bundle variant ID", required = true)
            @PathVariable UUID variantId) {
        List<BundleComponentResponse> components = bundleOptionService.getBundleComponents(variantId);
        return ResponseEntity.ok(ApiResponse.success("Bundle components retrieved successfully", components));
    }

    @GetMapping("/variants/{variantId}/policy")
    @Operation(summary = "Get variant policy", description = "Retrieve policy info for a product variant (perishable, return policy, advance payment)")
    public ResponseEntity<ApiResponse<ProductVariantPolicyResponse>> getProductVariantPolicy(
            @Parameter(description = "Variant ID", required = true)
            @PathVariable UUID variantId) {
        ProductVariantPolicyResponse policy = bundleOptionService.getProductVariantPolicy(variantId);
        return ResponseEntity.ok(ApiResponse.success("Variant policy retrieved successfully", policy));
    }

    @GetMapping("/variants/{variantId}/gift-box-detail")
    @Operation(summary = "Get gift box detail", description = "Retrieve gift box dimensions and weight info for a variant")
    public ResponseEntity<ApiResponse<GiftBoxVariantDetailResponse>> getGiftBoxVariantDetail(
            @Parameter(description = "Variant ID", required = true)
            @PathVariable UUID variantId) {
        GiftBoxVariantDetailResponse detail = bundleOptionService.getGiftBoxVariantDetail(variantId);
        return ResponseEntity.ok(ApiResponse.success("Gift box detail retrieved successfully", detail));
    }
}
