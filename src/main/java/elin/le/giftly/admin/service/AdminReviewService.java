package elin.le.giftly.admin.service;

import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.common.enums.ReviewStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.review.dto.ReviewResponse;
import elin.le.giftly.review.entity.Review;
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(
            UUID productId,
            UUID userId,
            ReviewStatus status,
            Pageable pageable) {
        
        if (productId != null && status != null) {
            return reviewRepository.findByProductIdAndStatus(productId, status, pageable).map(this::toReviewResponse);
        }
        if (productId != null) {
            return reviewRepository.findByProductId(productId, pageable).map(this::toReviewResponse);
        }
        if (userId != null) {
            return reviewRepository.findByUserId(userId, pageable).map(this::toReviewResponse);
        }
        if (status != null) {
            return reviewRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(this::toReviewResponse);
        }
        return reviewRepository.findAll(pageable).map(this::toReviewResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProduct(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(this::toReviewResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUser(UUID userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable).map(this::toReviewResponse);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return toReviewResponse(review);
    }

    @Transactional
    public void updateReviewStatus(UUID reviewId, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        
        review.setStatus(status);
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Delete associated image from Cloudinary if exists
        if (review.getReviewImage() != null && review.getReviewImage().getPublicId() != null) {
            cloudinaryService.deleteImage(review.getReviewImage().getPublicId());
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse toReviewResponse(Review review) {
        User user = userRepository.findById(review.getUserId()).orElse(null);

        ReviewResponse.ProductInfo productInfo = null;
        if (review.getProduct() != null) {
            String thumbnailUrl = getThumbnailUrl(review.getProduct());
            productInfo = new ReviewResponse.ProductInfo(
                    review.getProduct().getId(),
                    review.getProduct().getName(),
                    thumbnailUrl
            );
        }

        String reviewImageUrl = null;
        if (review.getReviewImage() != null) {
            reviewImageUrl = review.getReviewImage().getImageUrl();
        }

        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                user != null ? user.getEmail() : null,
                productInfo,
                review.getRating() != null ? review.getRating().intValue() : null,
                review.getTitle(),
                review.getComment(),
                review.getStatus(),
                reviewImageUrl,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private String getThumbnailUrl(elin.le.giftly.product.entity.Product product) {
        if (product == null || product.getImages() == null) return null;
        return product.getImages().stream()
                .filter(ProductImage::getIsThumbnail)
                .map(ProductImage::getSecureUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().stream()
                        .map(ProductImage::getSecureUrl)
                        .findFirst()
                        .orElse(null));
    }
}
