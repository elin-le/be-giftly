package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.service.AdminReviewService;
import elin.le.giftly.common.dto.ApiResponse;
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
@Tag(name = "Admin - Reviews", description = "Admin APIs for review management. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    @Operation(summary = "Get all reviews", description = "Retrieve all reviews with optional filtering by product or user with pagination")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAllReviews(
            @Parameter(description = "Filter by product ID")
            @RequestParam(required = false) UUID productId,
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) UUID userId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<ReviewResponse> reviews = adminReviewService.getAllReviews(productId, userId, pageable);
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

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete review", description = "Delete a review by its ID. Associated images will also be removed from Cloudinary.")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "Review ID to delete", required = true)
            @PathVariable UUID reviewId) {
        adminReviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
