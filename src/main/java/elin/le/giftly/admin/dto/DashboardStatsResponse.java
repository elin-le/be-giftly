package elin.le.giftly.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DashboardStatsResponse(
    long totalProducts,
    long totalCategories,
    long totalOrders,
    long totalUsers,
    long totalReviews,
    BigDecimal totalRevenue,
    long pendingOrders,
    long lowStockProducts
) {}
