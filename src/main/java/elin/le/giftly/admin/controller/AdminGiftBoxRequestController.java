package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.AdminGiftBoxRequestResponse;
import elin.le.giftly.admin.dto.CreateAdminGiftBoxRequest;
import elin.le.giftly.admin.dto.ReviewGiftBoxRequest;
import elin.le.giftly.admin.service.AdminGiftBoxRequestService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.dto.PageResponse;
import elin.le.giftly.common.enums.GiftBoxReviewStatus;
import elin.le.giftly.giftbox.dto.UpdateGiftBoxRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/gift-box-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Gift Box Requests", description = "Admin APIs for managing customer custom gift box requests")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminGiftBoxRequestController {

    private final AdminGiftBoxRequestService adminGiftBoxRequestService;

    @PostMapping
    @Operation(summary = "Create a custom gift box request for a customer")
    public ResponseEntity<ApiResponse<AdminGiftBoxRequestResponse>> createRequest(
            @Valid @RequestBody CreateAdminGiftBoxRequest request) {
        AdminGiftBoxRequestResponse created = adminGiftBoxRequestService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gift box request created successfully", created));
    }

    @GetMapping
    @Operation(summary = "List customer gift box requests")
    public ResponseEntity<ApiResponse<PageResponse<AdminGiftBoxRequestResponse>>> getRequests(
            @RequestParam(required = false) GiftBoxReviewStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminGiftBoxRequestResponse> requests = adminGiftBoxRequestService.getRequests(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(requests)));
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get a customer gift box request")
    public ResponseEntity<ApiResponse<AdminGiftBoxRequestResponse>> getRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(ApiResponse.success(adminGiftBoxRequestService.getRequestById(requestId)));
    }

    @PutMapping("/{requestId}")
    @Operation(summary = "Replace the gift box configuration and return it to pending review")
    public ResponseEntity<ApiResponse<AdminGiftBoxRequestResponse>> updateRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateGiftBoxRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Gift box request updated successfully",
                adminGiftBoxRequestService.updateRequest(requestId, request)));
    }

    @PatchMapping("/{requestId}/review")
    @Operation(summary = "Set review status and note for a gift box request")
    public ResponseEntity<ApiResponse<AdminGiftBoxRequestResponse>> reviewRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody ReviewGiftBoxRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Gift box request review updated successfully",
                adminGiftBoxRequestService.reviewRequest(requestId, request)));
    }

    @DeleteMapping("/{requestId}")
    @Operation(summary = "Delete an unused gift box request")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable UUID requestId) {
        adminGiftBoxRequestService.deleteRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success("Gift box request deleted successfully", null));
    }
}