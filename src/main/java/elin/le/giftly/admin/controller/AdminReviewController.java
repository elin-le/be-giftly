package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.service.AdminReviewService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.review.entity.Review;
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
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Review>>> getAllReviews(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Review> reviews;
        if (productId != null) {
            reviews = adminReviewService.getReviewsByProduct(productId, pageable);
        } else if (userId != null) {
            reviews = adminReviewService.getReviewsByUser(userId, pageable);
        } else {
            reviews = adminReviewService.getAllReviews(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Review>> getReviewById(@PathVariable UUID reviewId) {
        Review review = adminReviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable UUID reviewId) {
        adminReviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
