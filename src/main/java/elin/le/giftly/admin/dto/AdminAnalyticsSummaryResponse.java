package elin.le.giftly.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminAnalyticsSummaryResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalRevenue,
        BigDecimal averageOrderValue,
        long paidOrderCount,
        long totalOrderCount,
        long customerCount
) {}