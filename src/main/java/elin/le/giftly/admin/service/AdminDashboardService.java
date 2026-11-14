package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.DashboardStatsResponse;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.payment.repository.PaymentRepository;
import elin.le.giftly.product.repository.CategoryRepository;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.review.repository.ReviewRepository;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalReviews = reviewRepository.count();
        long pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long lowStockProducts = productRepository.findAll(org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
            .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 5)
            .count();

        BigDecimal totalRevenue = BigDecimal.ZERO;

        return new DashboardStatsResponse(
            totalProducts,
            totalCategories,
            totalOrders,
            totalUsers,
            totalReviews,
            totalRevenue,
            pendingOrders,
            lowStockProducts
        );
    }
}
