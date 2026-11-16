package elin.le.giftly.review.service;

import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.entity.OrderItem;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.review.dto.CreateReviewRequest;
import elin.le.giftly.review.dto.ReviewResponse;
import elin.le.giftly.review.entity.Review;
import elin.le.giftly.review.entity.ReviewImage;
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final Cloudinary cloudinary;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, MultipartFile imageFile) {
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

        // Build the review
        Review review = Review.builder()
                .userId(userId)
                .product(orderItem.getOrder() != null ? null : null)
                .productVariantId(orderItem.getProductVariantId())
                .orderItem(orderItem)
                .rating(request.rating().shortValue())
                .title(request.title())
                .comment(request.comment())
                .build();

        // Set product from variant if available
        if (orderItem.getProductVariantId() != null) {
            // Product is referenced via variant snapshot
        }

        review = reviewRepository.save(review);

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            uploadReviewImage(review, imageFile);
        }

        return toReviewResponse(review);
    }

    private void uploadReviewImage(Review review, MultipartFile imageFile) {
        try {
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", "giftly/reviews");
            
            Map<String, Object> uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), uploadParams);
            
            ReviewImage reviewImage = ReviewImage.builder()
                    .review(review)
                    .imageUrl((String) uploadResult.get("secure_url"))
                    .secureUrl((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .build();
            
            review.getImages().add(reviewImage);
            reviewRepository.save(review);
            log.info("Successfully uploaded review image for review: {}", review.getId());
        } catch (Exception e) {
            log.error("Failed to upload review image for review: {}", review.getId(), e);
            throw new BadRequestException("Failed to upload review image");
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(UUID productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        return reviews.stream().map(this::toReviewResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProductPaginated(UUID productId, Pageable pageable) {
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
            String thumbnailUrl = getThumbnailUrl(product);
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

    private String getThumbnailUrl(Product product) {
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

    private UUID getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
