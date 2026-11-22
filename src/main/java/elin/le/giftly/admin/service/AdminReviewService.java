package elin.le.giftly.admin.service;

import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.review.dto.ReviewResponse;
import elin.le.giftly.review.entity.Review;
import elin.le.giftly.review.entity.ReviewImage;
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(UUID productId, UUID userId, Pageable pageable) {
        if (productId != null && userId != null) {
            return reviewRepository.findByProductIdAndUserId(productId, userId, pageable).map(this::toReviewResponse);
        }
        if (productId != null) {
            return reviewRepository.findByProductId(productId, pageable).map(this::toReviewResponse);
        }
        if (userId != null) {
            return reviewRepository.findByUserId(userId, pageable).map(this::toReviewResponse);
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
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        for (ReviewImage img : review.getImages()) {
            if (img.getPublicId() != null) {
                cloudinaryService.deleteImage(img.getPublicId());
            }
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse toReviewResponse(Review review) {
        User user = userRepository.findById(review.getUserId()).orElse(null);

        ReviewResponse.ProductInfo productInfo = null;
        if (review.getProduct() != null) {
            String thumbnailUrl = getThumbnailUrl(review.getProductVariantId());
            productInfo = new ReviewResponse.ProductInfo(
                    review.getProduct().getId(),
                    review.getProduct().getName(),
                    thumbnailUrl
            );
        }

        List<String> imageUrls = review.getImages().stream()
                .map(ReviewImage::getSecureUrl)
                .toList();

        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                user != null ? user.getEmail() : null,
                productInfo,
                review.getProductVariantId(),
                review.getRating() != null ? review.getRating().intValue() : null,
                review.getTitle(),
                review.getComment(),
                imageUrls,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private String getThumbnailUrl(UUID productVariantId) {
        if (productVariantId == null) return null;
        ProductVariant variant = productVariantRepository.findById(productVariantId).orElse(null);
        if (variant == null || variant.getImages() == null || variant.getImages().isEmpty()) return null;
        return variant.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(elin.le.giftly.product.entity.ProductImage::getSecureUrl)
                .findFirst()
                .orElseGet(() -> variant.getImages().stream()
                        .map(elin.le.giftly.product.entity.ProductImage::getSecureUrl)
                        .findFirst()
                        .orElse(null));
    }
}
