package elin.le.giftly.review.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.review.dto.CreateReviewRequest;
import elin.le.giftly.review.dto.ReviewResponse;
import elin.le.giftly.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review APIs for customers (create, read, delete own reviews)")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create review", description = "Create a new product review with optional image upload. Uses multipart/form-data. Review is created with PENDING status.")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Parameter(description = "Review data: rating (1-5), optional title, optional comment", required = true)
            @RequestPart("request") @Valid CreateReviewRequest request,
            @Parameter(description = "Optional review image (JPEG, PNG)")
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ReviewResponse review = reviewService.createReview(request, image);
        return ResponseEntity.ok(ApiResponse.success("Review created successfully", review));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews by product", description = "Get all reviews for a specific product")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    @GetMapping("/product/{productId}/paginated")
    @Operation(summary = "Get paginated reviews by product", description = "Get paginated reviews for a product with optional rating filter")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByProductPaginated(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Filter by rating (1-5)")
            @RequestParam(required = false) Short rating,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ReviewResponse> reviews;
        if (rating != null) {
            reviews = reviewService.getReviewsByProductAndRating(productId, rating, pageable);
        } else {
            reviews = reviewService.getReviewsByProductPaginated(productId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my reviews", description = "Get paginated list of reviews created by the current user")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getMyReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success("Your reviews retrieved successfully", reviews));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Retrieve a specific review by its ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "Review ID", required = true)
            @PathVariable UUID reviewId) {
        ReviewResponse review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review retrieved successfully", review));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete my review", description = "Delete a review that belongs to the current user")
    public ResponseEntity<ApiResponse<Void>> deleteMyReview(
            @Parameter(description = "Review ID to delete", required = true)
            @PathVariable UUID reviewId) {
        reviewService.deleteMyReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
