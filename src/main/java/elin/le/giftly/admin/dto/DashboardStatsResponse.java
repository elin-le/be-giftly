package elin.le.giftly.admin.dto;

import java.math.BigDecimal;

public record DashboardStatsResponse(
    long totalProducts,
    long totalCategories,
    long totalOrders,
    long totalUsers,
    long totalReviews,
    BigDecimal totalRevenue,
    BigDecimal averageOrderValue,
    long paidOrderCount,
    long pendingOrders,
    long lowStockProducts
) {}
