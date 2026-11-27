package elin.le.giftly.giftbox.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxItemRequest;
import elin.le.giftly.giftbox.dto.GiftBoxComponentRequest;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.service.GiftBoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gift-boxes")
@RequiredArgsConstructor
@Tag(name = "Gift Boxes", description = "Gift box creation and management APIs for customers")
public class GiftBoxController {

    private final GiftBoxService giftBoxService;

    @PostMapping
    @Operation(summary = "Create gift box", description = "Create a new custom gift box with selected products")
    public ResponseEntity<ApiResponse<GiftBoxResponse>> createGiftBox(
            @Parameter(description = "Gift box data with name, description, and items", required = true)
            @Valid @RequestBody CreateGiftBoxRequest request) {
        GiftBoxResponse giftBox = giftBoxService.createGiftBox(request);
        return ResponseEntity.ok(ApiResponse.success("Gift box created successfully", giftBox));
    }

    @GetMapping
    @Operation(summary = "Get my gift boxes", description = "Retrieve all gift boxes created by the current user")
    public ResponseEntity<ApiResponse<List<GiftBoxResponse>>> getMyGiftBoxes() {
        List<GiftBoxResponse> giftBoxes = giftBoxService.getMyGiftBoxes();
        return ResponseEntity.ok(ApiResponse.success("Gift boxes retrieved successfully", giftBoxes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get gift box by ID", description = "Retrieve details of a specific gift box")
    public ResponseEntity<ApiResponse<GiftBoxResponse>> getGiftBoxById(
            @Parameter(description = "Gift box ID", required = true)
            @PathVariable UUID id) {
        GiftBoxResponse giftBox = giftBoxService.getGiftBoxById(id);
        return ResponseEntity.ok(ApiResponse.success("Gift box retrieved successfully", giftBox));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete gift box", description = "Delete a gift box created by the current user")
    public ResponseEntity<ApiResponse<Void>> deleteGiftBox(
            @Parameter(description = "Gift box ID to delete", required = true)
            @PathVariable UUID id) {
        giftBoxService.deleteGiftBox(id);
        return ResponseEntity.ok(ApiResponse.success("Gift box deleted successfully", null));
    }

    @PostMapping("/{giftBoxId}/items")
    @Operation(summary = "Add item to gift box", description = "Add a product item to an existing gift box")
    public ResponseEntity<ApiResponse<GiftBoxResponse>> addGiftBoxItem(
            @Parameter(description = "Gift box ID", required = true)
            @PathVariable UUID giftBoxId,
            @Parameter(description = "Item to add with productVariantId and quantity", required = true)
            @Valid @RequestBody GiftBoxItemRequest request) {
        GiftBoxResponse giftBox = giftBoxService.addGiftBoxItem(giftBoxId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to gift box", giftBox));
    }

    @PostMapping("/{giftBoxId}/components")
    @Operation(summary = "Add a custom component", description = "Add one product variant for its component type")
    public ResponseEntity<ApiResponse<GiftBoxResponse>> addGiftBoxComponent(
            @Parameter(description = "Gift box ID", required = true)
            @PathVariable UUID giftBoxId,
            @Valid @RequestBody GiftBoxComponentRequest request) {
        GiftBoxResponse giftBox = giftBoxService.addGiftBoxComponent(giftBoxId, request);
        return ResponseEntity.ok(ApiResponse.success("Component added to gift box", giftBox));
    }

    @DeleteMapping("/{giftBoxId}/components/{componentType}")
    @Operation(summary = "Remove a custom component", description = "Remove the selected product for a component type")
    public ResponseEntity<ApiResponse<GiftBoxResponse>> removeGiftBoxComponent(
            @Parameter(description = "Gift box ID", required = true)
            @PathVariable UUID giftBoxId,
            @PathVariable String componentType) {
        GiftBoxResponse giftBox = giftBoxService.removeGiftBoxComponent(giftBoxId, componentType);
        return ResponseEntity.ok(ApiResponse.success("Component removed from gift box", giftBox));
    }

    @DeleteMapping("/{giftBoxId}/items/{itemId}")
    @Operation(summary = "Remove item from gift box", description = "Remove a product item from a gift box")
    public ResponseEntity<ApiResponse<GiftBoxResponse>> removeGiftBoxItem(
            @Parameter(description = "Gift box ID", required = true)
            @PathVariable UUID giftBoxId,
            @Parameter(description = "Item ID to remove", required = true)
            @PathVariable UUID itemId) {
        GiftBoxResponse giftBox = giftBoxService.removeGiftBoxItem(giftBoxId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from gift box", giftBox));
    }
}
