package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.DashboardStatsResponse;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.payment.repository.PaymentRepository;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.repository.CategoryRepository;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDashboardService Unit Tests")
class AdminDashboardServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Nested
    @DisplayName("getDashboardStats")
    class GetDashboardStats {

        @Test
        @DisplayName("Should return dashboard stats")
        void shouldReturnDashboardStats() {
            when(productRepository.count()).thenReturn(10L);
            when(categoryRepository.count()).thenReturn(5L);
            when(orderRepository.count()).thenReturn(100L);
            when(userRepository.count()).thenReturn(50L);
            when(reviewRepository.count()).thenReturn(200L);
            when(orderRepository.findByStatus(OrderStatus.PENDING_CONFIRMATION, Pageable.unpaged()))
                    .thenReturn(new PageImpl<>(List.of()));
            Product product = mock(Product.class);
            when(product.getStockQuantity()).thenReturn(3);
            when(productRepository.findAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(product)));

            DashboardStatsResponse stats = adminDashboardService.getDashboardStats();

            assertNotNull(stats);
            assertEquals(10, stats.totalProducts());
            assertEquals(5, stats.totalCategories());
            assertEquals(100, stats.totalOrders());
            assertEquals(50, stats.totalUsers());
            assertEquals(200, stats.totalReviews());
            assertEquals(0, stats.pendingOrders());
            assertEquals(1, stats.lowStockProducts());
        }

        @Test
        @DisplayName("Should return zero stats when empty")
        void shouldReturnZeroStats() {
            when(productRepository.count()).thenReturn(0L);
            when(categoryRepository.count()).thenReturn(0L);
            when(orderRepository.count()).thenReturn(0L);
            when(userRepository.count()).thenReturn(0L);
            when(reviewRepository.count()).thenReturn(0L);
            when(orderRepository.findByStatus(any(), any())).thenReturn(new PageImpl<>(List.of()));
            when(productRepository.findAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of()));

            DashboardStatsResponse stats = adminDashboardService.getDashboardStats();

            assertNotNull(stats);
            assertEquals(0, stats.totalProducts());
            assertEquals(0, stats.totalCategories());
        }
    }
}
