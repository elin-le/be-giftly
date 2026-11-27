package elin.le.giftly.review.service;

import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.exception.model.BadRequestException;
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
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @InjectMocks
    private ReviewService reviewService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsReviewWithThreeCloudinaryImageReferences() {
        UUID userId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        User user = User.builder().id(userId).email("customer@example.test").build();
        Product product = Product.builder().id(UUID.randomUUID()).name("Gift product").build();
        ProductVariant variant = ProductVariant.builder().id(variantId).product(product).build();
        Order order = Order.builder().userId(userId).status(OrderStatus.COMPLETED).build();
        OrderItem orderItem = OrderItem.builder().order(order).productVariantId(variantId).build();

        authenticate(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItem));
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            if (review.getId() == null) {
                review.setId(UUID.randomUUID());
            }
            return review;
        });
        List<ReviewImageRequest> images = List.of(
                imageReference("review-1"), imageReference("review-2"), imageReference("review-3"));
        ReviewResponse response = reviewService.createReview(
                new CreateReviewRequest(orderItemId, 5, "Great", "Lovely gift", images));

        assertEquals(3, response.imageUrls().size());
        assertEquals(List.of("https://cdn.example.test/review-1", "https://cdn.example.test/review-2",
                "https://cdn.example.test/review-3"), response.imageUrls());
        verify(reviewRepository).save(org.mockito.ArgumentMatchers.argThat(review ->
                review.getImages().size() == 3
                        && review.getImages().get(0).getPublicId().equals("giftly/reviews/review-1")
                        && review.getImages().get(0).getSecureUrl().equals("https://cdn.example.test/review-1")));
        verify(cloudinaryService, never()).deleteImage(any());
    }

    @Test
    void rejectsMoreThanThreeImagesBeforeCreatingReview() {
        List<ReviewImageRequest> images = List.of(
                imageReference("one"), imageReference("two"), imageReference("three"), imageReference("four"));
        assertThrows(BadRequestException.class, () -> reviewService.createReview(
                new CreateReviewRequest(UUID.randomUUID(), 5, null, null, images)));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void rejectsImageReferenceWithoutHttpsUrlBeforeCreatingReview() {
        List<ReviewImageRequest> images = List.of(new ReviewImageRequest("giftly/reviews/one", "http://example.test/one"));

        assertThrows(BadRequestException.class, () -> reviewService.createReview(
                new CreateReviewRequest(UUID.randomUUID(), 5, null, null, images)));

        verify(reviewRepository, never()).save(any());
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "test", List.of()));
    }

    private ReviewImageRequest imageReference(String name) {
        return new ReviewImageRequest("giftly/reviews/" + name, "https://cdn.example.test/" + name);
    }
}