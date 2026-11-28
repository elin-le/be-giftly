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
import java.math.RoundingMode;

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
        long pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING_CONFIRMATION, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        // Low stock check moved to ProductVariant level
        long lowStockProducts = 0;

        BigDecimal totalRevenue = paymentRepository.sumPaidAmount();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        long paidOrderCount = paymentRepository.countDistinctPaidOrders();
        BigDecimal paidOrderTotals = orderRepository.sumPaidOrderTotals();
        if (paidOrderTotals == null) {
            paidOrderTotals = BigDecimal.ZERO;
        }
        BigDecimal averageOrderValue = paidOrderCount == 0
                ? BigDecimal.ZERO
                : paidOrderTotals.divide(BigDecimal.valueOf(paidOrderCount), 2, RoundingMode.HALF_UP);

        return new DashboardStatsResponse(
            totalProducts,
            totalCategories,
            totalOrders,
            totalUsers,
            totalReviews,
            totalRevenue,
            averageOrderValue,
            paidOrderCount,
            pendingOrders,
            lowStockProducts
        );
    }
}
