package elin.le.giftly.review.service;

import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.entity.OrderItem;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.review.dto.CreateReviewRequest;
import elin.le.giftly.review.dto.ReviewImageRequest;
import elin.le.giftly.review.dto.ReviewResponse;
import elin.le.giftly.review.entity.Review;
import elin.le.giftly.review.entity.ReviewImage;
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        List<ReviewImageRequest> images = validateReviewImages(request.images());
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        // Find the order item
        OrderItem orderItem = orderItemRepository.findById(request.orderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", request.orderItemId()));

        Order order = orderItem.getOrder();
        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only review items from your own orders");
        }

        if (order.getStatus() != OrderStatus.COMPLETED && order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException(
                    "You can only review items from completed or delivered orders. Current status: " + order.getStatus()
            );
        }

        if (orderItem.getProductVariantId() == null) {
            throw new BadRequestException("Only purchased product variants can be reviewed");
        }
        ProductVariant productVariant = productVariantRepository.findById(orderItem.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductVariant", "id", orderItem.getProductVariantId()));

        // Build the review
        Review review = Review.builder()
                .userId(userId)
                .product(productVariant.getProduct())
                .productVariantId(orderItem.getProductVariantId())
                .orderItem(orderItem)
                .rating(request.rating().shortValue())
                .title(request.title())
                .comment(request.comment())
                .build();

        for (ReviewImageRequest image : images) {
            review.getImages().add(ReviewImage.builder()
                    .review(review)
                    .secureUrl(image.secureUrl())
                    .publicId(image.publicId())
                    .build());
        }

        review = reviewRepository.save(review);
        return toReviewResponse(review);
    }

    private List<ReviewImageRequest> validateReviewImages(List<ReviewImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        if (images.size() > 3) {
            throw new BadRequestException("A review can have at most 3 images");
        }

        for (ReviewImageRequest image : images) {
            if (image == null || image.publicId() == null || image.publicId().isBlank()
                    || image.secureUrl() == null || !image.secureUrl().startsWith("https://")) {
                throw new BadRequestException("Each review image must include a public ID and HTTPS secure URL");
            }
        }
        return images;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProduct(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(this::toReviewResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProductAndRating(UUID productId, Short rating, Pageable pageable) {
        return reviewRepository.findByProductIdAndRating(productId, rating, pageable).map(this::toReviewResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toReviewResponse);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return toReviewResponse(review);
    }

    @Transactional
    public void deleteMyReview(UUID reviewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own reviews");
        }

        // Delete associated images from Cloudinary
        for (ReviewImage img : review.getImages()) {
            if (img.getPublicId() != null) {
                cloudinaryService.deleteImage(img.getPublicId());
            }
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse toReviewResponse(Review review) {
        User user = userRepository.findById(review.getUserId()).orElse(null);
        Product product = review.getProduct();

        ReviewResponse.ProductInfo productInfo = null;
        if (product != null) {
            String thumbnailUrl = getThumbnailUrl(review.getProductVariantId());
            productInfo = new ReviewResponse.ProductInfo(
                    product.getId(),
                    product.getName(),
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

    private UUID getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
