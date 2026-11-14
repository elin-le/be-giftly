package elin.le.giftly.review.service;

import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.common.enums.ReviewStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.entity.OrderItem;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.review.dto.CreateReviewRequest;
import elin.le.giftly.review.dto.ReviewResponse;
import elin.le.giftly.review.entity.Review;
import elin.le.giftly.review.entity.ReviewImage;
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private Cloudinary cloudinary;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Category testCategory;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        testCategory = Category.builder().name("Test Category").build();
        testCategory.setId(UUID.randomUUID());

        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .category(testCategory)
                .name("Test Product")
                .price(new BigDecimal("100000"))
                .build();

        UUID orderId = UUID.randomUUID();
        testOrder = Order.builder()
                .id(orderId)
                .userId(testUserId)
                .status(OrderStatus.COMPLETED)
                .build();

        testOrderItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .order(testOrder)
                .build();
    }


    private void mockCurrentUserContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn("test@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    @Nested
    @DisplayName("createReview")
    class CreateReview {

        @BeforeEach
        void setUpCreate() {
            mockCurrentUserContext();
        }

        @Test
        @DisplayName("Should create review successfully")
        void shouldCreateReviewSuccessfully() {
            UUID orderItemId = testOrderItem.getId();
            CreateReviewRequest request = new CreateReviewRequest(orderItemId, 5, "Great!", "Excellent", null);

            when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(testOrderItem));
            when(reviewRepository.existsByOrderItemId(orderItemId)).thenReturn(false);
            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ReviewResponse response = reviewService.createReview(request, null);

            assertNotNull(response);
            assertEquals(5, response.rating());
            assertEquals("Great!", response.title());
            assertEquals(ReviewStatus.PENDING, response.status());
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("Should throw when order item not found")
        void shouldThrowWhenOrderItemNotFound() {
            UUID orderItemId = UUID.randomUUID();
            CreateReviewRequest request = new CreateReviewRequest(orderItemId, 5, "Title", "Comment", null);

            when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> reviewService.createReview(request, null));
        }

        @Test
        @DisplayName("Should throw when order not completed")
        void shouldThrowWhenOrderNotCompleted() {
            testOrder.setStatus(OrderStatus.PENDING_CONFIRMATION);
            UUID orderItemId = testOrderItem.getId();
            CreateReviewRequest request = new CreateReviewRequest(orderItemId, 5, "Title", "Comment", null);

            when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(testOrderItem));

            assertThrows(BadRequestException.class, () -> reviewService.createReview(request, null));
        }

        @Test
        @DisplayName("Should throw when review already exists")
        void shouldThrowWhenReviewAlreadyExists() {
            UUID orderItemId = testOrderItem.getId();
            CreateReviewRequest request = new CreateReviewRequest(orderItemId, 5, "Title", "Comment", null);

            when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(testOrderItem));
            when(reviewRepository.existsByOrderItemId(orderItemId)).thenReturn(true);

            assertThrows(BadRequestException.class, () -> reviewService.createReview(request, null));
        }

        @Test
        @DisplayName("Should throw when order belongs to another user")
        void shouldThrowWhenOrderBelongsToAnotherUser() {
            UUID otherUserId = UUID.randomUUID();
            testOrder.setUserId(otherUserId);
            UUID orderItemId = testOrderItem.getId();
            CreateReviewRequest request = new CreateReviewRequest(orderItemId, 5, "Title", "Comment", null);

            when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(testOrderItem));

            assertThrows(AccessDeniedException.class, () -> reviewService.createReview(request, null));
        }

        @Test
        @DisplayName("Should create review with imageUrl from request")
        void shouldCreateReviewWithImageUrl() {
            UUID orderItemId = testOrderItem.getId();
            CreateReviewRequest request = new CreateReviewRequest(orderItemId, 5, "Title", "Comment", "https://test.com/img.jpg");
            when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(testOrderItem));
            when(reviewRepository.existsByOrderItemId(orderItemId)).thenReturn(false);
            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ReviewResponse response = reviewService.createReview(request, null);

            assertNotNull(response);
            assertEquals("https://test.com/img.jpg", response.reviewImageUrl());
        }
    }

    @Nested
    @DisplayName("getReviewsByProduct")
    class GetReviewsByProduct {

        @Test
        @DisplayName("Should return reviews for product")
        void shouldReturnReviewsForProduct() {
            UUID productId = testProduct.getId();
            List<Review> reviews = List.of(createReview());
            when(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)).thenReturn(reviews);
            when(userRepository.findById(any())).thenReturn(Optional.of(testUser));

            List<ReviewResponse> response = reviewService.getReviewsByProduct(productId);

            assertEquals(1, response.size());
            assertEquals(5, response.get(0).rating());
        }

        @Test
        @DisplayName("Should return empty list when no reviews")
        void shouldReturnEmptyWhenNoReviews() {
            UUID productId = testProduct.getId();
            when(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)).thenReturn(Collections.emptyList());

            List<ReviewResponse> response = reviewService.getReviewsByProduct(productId);

            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("getMyReviews")
    class GetMyReviews {

        @BeforeEach
        void setUp() {
            mockCurrentUserContext();
        }

        @Test
        @DisplayName("Should return paginated reviews for current user")
        void shouldReturnPaginatedReviews() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Review> reviews = List.of(createReview());
            Page<Review> reviewPage = new PageImpl<>(reviews);
            when(reviewRepository.findByUserIdOrderByCreatedAtDesc(testUserId, pageable)).thenReturn(reviewPage);
            when(userRepository.findById(any())).thenReturn(Optional.of(testUser));

            Page<ReviewResponse> response = reviewService.getMyReviews(pageable);

            assertEquals(1, response.getTotalElements());
        }
    }

    @Nested
    @DisplayName("getReviewById")
    class GetReviewById {

        @Test
        @DisplayName("Should return review by ID")
        void shouldReturnReviewById() {
            Review review = createReview();
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
            when(userRepository.findById(any())).thenReturn(Optional.of(testUser));

            ReviewResponse response = reviewService.getReviewById(review.getId());

            assertNotNull(response);
            assertEquals(review.getId(), response.id());
        }

        @Test
        @DisplayName("Should throw when review not found")
        void shouldThrowWhenNotFound() {
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(reviewId));
        }
    }

    @Nested
    @DisplayName("deleteMyReview")
    class DeleteMyReview {

        @BeforeEach
        void setUp() {
            mockCurrentUserContext();
        }

        @Test
        @DisplayName("Should delete review successfully")
        void shouldDeleteReviewSuccessfully() {
            Review review = createReview();
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

            reviewService.deleteMyReview(review.getId());

            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("Should delete review with image cleanup")
        void shouldDeleteReviewWithImage() {
            Review review = createReview();
            ReviewImage image = ReviewImage.builder().publicId("test-public-id").build();
            review.setReviewImage(image);
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

            reviewService.deleteMyReview(review.getId());

            verify(cloudinaryService).deleteImage("test-public-id");
            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("Should throw when deleting another user's review")
        void shouldThrowWhenDeletingOthersReview() {
            Review review = createReview();
            review.setUserId(UUID.randomUUID());
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

            assertThrows(AccessDeniedException.class, () -> reviewService.deleteMyReview(review.getId()));
        }
    }

    private Review createReview() {
        Review review = Review.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .product(testProduct)
                .orderItem(testOrderItem)
                .rating((short) 5)
                .title("Great product!")
                .comment("Excellent quality")
                .status(ReviewStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return review;
    }
}
