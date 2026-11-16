package elin.le.giftly.admin.service;

import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.review.dto.ReviewResponse;
import elin.le.giftly.review.entity.Review;
import elin.le.giftly.review.entity.ReviewImage;
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminReviewService Unit Tests")
class AdminReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private CloudinaryService cloudinaryService;
    @InjectMocks private AdminReviewService adminReviewService;

    private UUID testUserId;
    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder().id(testUserId).email("user@test.com").build();

        Category category = Category.builder().id(UUID.randomUUID()).name("Test Category").build();
        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .category(category)
                .productType(ProductType.REGULAR)
                .status(ProductStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("getAllReviews")
    class GetAllReviews {

        @Test
        @DisplayName("Should return all reviews")
        void shouldReturnAllReviews() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> emptyPage = new PageImpl<>(List.of());
            when(reviewRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<ReviewResponse> result = adminReviewService.getAllReviews(null, null, pageable);

            assertNotNull(result);
            verify(reviewRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should filter by product ID")
        void shouldFilterByProductId() {
            UUID productId = testProduct.getId();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> emptyPage = new PageImpl<>(List.of());
            when(reviewRepository.findByProductId(productId, pageable)).thenReturn(emptyPage);

            adminReviewService.getAllReviews(productId, null, pageable);

            verify(reviewRepository).findByProductId(productId, pageable);
        }

        @Test
        @DisplayName("Should filter by user ID")
        void shouldFilterByUserId() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> emptyPage = new PageImpl<>(List.of());
            when(reviewRepository.findByUserId(testUserId, pageable)).thenReturn(emptyPage);

            adminReviewService.getAllReviews(null, testUserId, pageable);

            verify(reviewRepository).findByUserId(testUserId, pageable);
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

            ReviewResponse response = adminReviewService.getReviewById(review.getId());

            assertNotNull(response);
            assertEquals(review.getId(), response.id());
        }

        @Test
        @DisplayName("Should throw when review not found")
        void shouldThrowWhenNotFound() {
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminReviewService.getReviewById(reviewId));
        }
    }

    @Nested
    @DisplayName("deleteReview")
    class DeleteReview {

        @Test
        @DisplayName("Should delete review")
        void shouldDeleteReview() {
            Review review = createReview();
            when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

            adminReviewService.deleteReview(review.getId());

            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("Should throw when review not found")
        void shouldThrowWhenNotFound() {
            UUID reviewId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminReviewService.deleteReview(reviewId));
        }
    }

    private Review createReview() {
        return Review.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .product(testProduct)
                .rating((short) 5)
                .title("Test Review")
                .comment("Test comment")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
