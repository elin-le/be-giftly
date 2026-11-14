package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.service.AdminReviewService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.review.dto.ReviewResponse;
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
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAllReviews(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ReviewResponse> reviews;
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
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable UUID reviewId) {
        ReviewResponse review = adminReviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.success(review));
    }
}
