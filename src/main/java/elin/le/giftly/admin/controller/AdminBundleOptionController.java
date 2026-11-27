package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.CreateBundleComponentRequest;
import elin.le.giftly.admin.dto.CreateBundleOptionGroupRequest;
import elin.le.giftly.admin.dto.UpdateBundleComponentRequest;
import elin.le.giftly.admin.dto.UpdateBundleOptionGroupRequest;
import elin.le.giftly.admin.service.AdminBundleOptionService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.product.dto.BundleComponentResponse;
import elin.le.giftly.product.dto.BundleOptionGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/bundle")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Bundle Options", description = "Admin APIs for bundle option groups and components CRUD. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminBundleOptionController {

    private final AdminBundleOptionService adminBundleOptionService;

    // Bundle Option Group endpoints

    @PostMapping("/option-groups")
    @Operation(summary = "Create bundle option group", description = "Create a new bundle option group with options for a gift box variant")
    public ResponseEntity<ApiResponse<BundleOptionGroupResponse>> createBundleOptionGroup(
            @Parameter(description = "Bundle option group data", required = true)
            @Valid @RequestBody CreateBundleOptionGroupRequest request) {
        BundleOptionGroupResponse response = adminBundleOptionService.createBundleOptionGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bundle option group created successfully", response));
    }

    @PutMapping("/option-groups/{groupId}")
    @Operation(summary = "Update bundle option group", description = "Update an existing bundle option group and its options")
    public ResponseEntity<ApiResponse<BundleOptionGroupResponse>> updateBundleOptionGroup(
            @Parameter(description = "Bundle option group ID", required = true)
            @PathVariable UUID groupId,
            @Parameter(description = "Updated bundle option group data", required = true)
            @Valid @RequestBody UpdateBundleOptionGroupRequest request) {
        BundleOptionGroupResponse response = adminBundleOptionService.updateBundleOptionGroup(groupId, request);
        return ResponseEntity.ok(ApiResponse.success("Bundle option group updated successfully", response));
    }

    @DeleteMapping("/option-groups/{groupId}")
    @Operation(summary = "Delete bundle option group", description = "Delete a bundle option group and its options")
    public ResponseEntity<ApiResponse<Void>> deleteBundleOptionGroup(
            @Parameter(description = "Bundle option group ID to delete", required = true)
            @PathVariable UUID groupId) {
        adminBundleOptionService.deleteBundleOptionGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success("Bundle option group deleted successfully", null));
    }

    @DeleteMapping("/options/{optionId}")
    @Operation(summary = "Delete bundle option", description = "Delete a specific bundle option")
    public ResponseEntity<ApiResponse<Void>> deleteBundleOption(
            @Parameter(description = "Bundle option ID to delete", required = true)
            @PathVariable UUID optionId) {
        adminBundleOptionService.deleteBundleOption(optionId);
        return ResponseEntity.ok(ApiResponse.success("Bundle option deleted successfully", null));
    }

    // Bundle Component endpoints

    @PostMapping("/components")
    @Operation(summary = "Create bundle component", description = "Add a component (box, product, accessory, greeting card) to a bundle variant")
    public ResponseEntity<ApiResponse<BundleComponentResponse>> createBundleComponent(
            @Parameter(description = "Bundle component data", required = true)
            @Valid @RequestBody CreateBundleComponentRequest request) {
        BundleComponentResponse response = adminBundleOptionService.createBundleComponent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bundle component created successfully", response));
    }

    @DeleteMapping("/components/{componentId}")
    @Operation(summary = "Delete bundle component", description = "Remove a component from a bundle variant")
    public ResponseEntity<ApiResponse<Void>> deleteBundleComponent(
            @Parameter(description = "Bundle component ID to delete", required = true)
            @PathVariable UUID componentId) {
        adminBundleOptionService.deleteBundleComponent(componentId);
        return ResponseEntity.ok(ApiResponse.success("Bundle component deleted successfully", null));
    }

    @PutMapping("/components/{componentId}")
    @Operation(summary = "Update bundle component", description = "Update a bundle component's role and quantity")
    public ResponseEntity<ApiResponse<BundleComponentResponse>> updateBundleComponent(
            @Parameter(description = "Bundle component ID", required = true)
            @PathVariable UUID componentId,
            @Parameter(description = "Updated bundle component data", required = true)
            @Valid @RequestBody UpdateBundleComponentRequest request) {
        BundleComponentResponse response = adminBundleOptionService.updateBundleComponent(componentId, request);
        return ResponseEntity.ok(ApiResponse.success("Bundle component updated successfully", response));
    }

    // List endpoints

    @GetMapping("/option-groups")
    @Operation(summary = "List all bundle option groups", description = "Retrieve all bundle option groups")
    public ResponseEntity<ApiResponse<List<BundleOptionGroupResponse>>> getAllBundleOptionGroups() {
        List<BundleOptionGroupResponse> groups = adminBundleOptionService.getAllBundleOptionGroups();
        return ResponseEntity.ok(ApiResponse.success("Bundle option groups retrieved successfully", groups));
    }

    @GetMapping("/variants/{variantId}/option-groups")
    @Operation(summary = "List bundle option groups by variant", description = "Retrieve all bundle option groups for a specific variant")
    public ResponseEntity<ApiResponse<List<BundleOptionGroupResponse>>> getBundleOptionGroupsByVariant(
            @Parameter(description = "Bundle variant ID", required = true)
            @PathVariable UUID variantId) {
        List<BundleOptionGroupResponse> groups = adminBundleOptionService.getBundleOptionGroupsByVariantId(variantId);
        return ResponseEntity.ok(ApiResponse.success("Bundle option groups retrieved successfully", groups));
    }

    @GetMapping("/components")
    @Operation(summary = "List all bundle components", description = "Retrieve all bundle components")
    public ResponseEntity<ApiResponse<List<BundleComponentResponse>>> getAllBundleComponents() {
        List<BundleComponentResponse> components = adminBundleOptionService.getAllBundleComponents();
        return ResponseEntity.ok(ApiResponse.success("Bundle components retrieved successfully", components));
    }

    @GetMapping("/variants/{variantId}/components")
    @Operation(summary = "List bundle components by variant", description = "Retrieve all bundle components for a specific variant")
    public ResponseEntity<ApiResponse<List<BundleComponentResponse>>> getBundleComponentsByVariant(
            @Parameter(description = "Bundle variant ID", required = true)
            @PathVariable UUID variantId) {
        List<BundleComponentResponse> components = adminBundleOptionService.getBundleComponentsByVariantId(variantId);
        return ResponseEntity.ok(ApiResponse.success("Bundle components retrieved successfully", components));
    }
}
