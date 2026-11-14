package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.service.AdminReviewService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.enums.ReviewStatus;
import elin.le.giftly.review.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Reviews", description = "Admin APIs for review management (approve/reject/delete). Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    @Operation(summary = "Get all reviews", description = "Retrieve all reviews with optional filtering by product, user, or status with pagination")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAllReviews(
            @Parameter(description = "Filter by product ID")
            @RequestParam(required = false) UUID productId,
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) UUID userId,
            @Parameter(description = "Filter by review status (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) ReviewStatus status,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<ReviewResponse> reviews = adminReviewService.getAllReviews(productId, userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Retrieve a specific review by its ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "Review ID", required = true)
            @PathVariable UUID reviewId) {
        ReviewResponse review = adminReviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    @PatchMapping("/{reviewId}/status")
    @Operation(summary = "Update review status", description = "Approve or reject a review. Admin can change status to APPROVED or REJECTED.")
    public ResponseEntity<ApiResponse<Void>> updateReviewStatus(
            @Parameter(description = "Review ID", required = true)
            @PathVariable UUID reviewId,
            @Parameter(description = "New status: APPROVED or REJECTED", required = true)
            @RequestParam ReviewStatus status) {
        adminReviewService.updateReviewStatus(reviewId, status);
        return ResponseEntity.ok(ApiResponse.success("Review status updated successfully", null));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete review", description = "Delete a review by its ID. Associated image will also be removed from Cloudinary.")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "Review ID to delete", required = true)
            @PathVariable UUID reviewId) {
        adminReviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
