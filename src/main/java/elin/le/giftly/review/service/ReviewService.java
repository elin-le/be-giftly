package elin.le.giftly.review.service;

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
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        // Find the order item
        OrderItem orderItem = orderItemRepository.findById(request.orderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", request.orderItemId()));

        // Verify the order item belongs to an order owned by the current user
        Order order = orderItem.getOrder();
        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only review items from your own orders");
        }

        // Check order status must be COMPLETED
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException(
                    "You can only review items from completed orders. Current status: " + order.getStatus()
            );
        }

        // Check if review already exists for this order item (one review per order item)
        if (reviewRepository.existsByOrderItemId(request.orderItemId())) {
            throw new BadRequestException("You have already reviewed this item");
        }

        // Build and save the review
        Review review = Review.builder()
                .userId(userId)
                .product(orderItem.getProduct())
                .orderItem(orderItem)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        review = reviewRepository.save(review);
        return toReviewResponse(review);
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
    public List<ReviewResponse> getMyReviews() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        Page<Review> reviews = reviewRepository.findByUserId(userId, Pageable.unpaged());
        return reviews.getContent().stream().map(this::toReviewResponse).toList();
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

        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                user != null ? user.getEmail() : null,
                productInfo,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
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
