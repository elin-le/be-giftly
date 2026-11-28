package elin.le.giftly.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminDailyRevenueResponse(
        LocalDate date,
        BigDecimal revenue,
        long paymentCount,
        long paidOrderCount
) {}