package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.AdminAnalyticsSummaryResponse;
import elin.le.giftly.admin.dto.AdminDailyRevenueResponse;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public AdminAnalyticsSummaryResponse getSummary(LocalDate from, LocalDate to) {
        DateRange range = resolveRange(from, to);
        LocalDateTime start = range.from().atStartOfDay();
        LocalDateTime endExclusive = range.to().plusDays(1).atStartOfDay();
        BigDecimal revenue = paymentRepository.sumPaidAmountBetween(start, endExclusive);
        if (revenue == null) {
            revenue = ZERO;
        }
        long paidOrderCount = paymentRepository.countDistinctPaidOrdersBetween(start, endExclusive);
        BigDecimal paidOrderTotals = orderRepository.sumPaidOrderTotalsBetween(start, endExclusive);
        if (paidOrderTotals == null) {
            paidOrderTotals = ZERO;
        }
        BigDecimal averageOrderValue = paidOrderCount == 0
                ? ZERO
                : paidOrderTotals.divide(BigDecimal.valueOf(paidOrderCount), 2, RoundingMode.HALF_UP);
        return new AdminAnalyticsSummaryResponse(
                range.from(),
                range.to(),
                revenue,
                averageOrderValue,
                paidOrderCount,
                orderRepository.countByOrderedAtGreaterThanEqualAndOrderedAtLessThan(start, endExclusive),
                orderRepository.countDistinctCustomersBetween(start, endExclusive)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminDailyRevenueResponse> getDailyRevenue(LocalDate from, LocalDate to) {
        DateRange range = resolveRange(from, to);
        LocalDateTime start = range.from().atStartOfDay();
        LocalDateTime endExclusive = range.to().plusDays(1).atStartOfDay();
        Map<LocalDate, PaymentRepository.DailyRevenueProjection> revenuesByDate = new HashMap<>();
        paymentRepository.findDailyPaidRevenue(start, endExclusive)
                .forEach(row -> revenuesByDate.put(row.getRevenueDate(), row));

        return range.from().datesUntil(range.to().plusDays(1))
                .map(date -> {
                    PaymentRepository.DailyRevenueProjection row = revenuesByDate.get(date);
                    return row == null
                            ? new AdminDailyRevenueResponse(date, ZERO, 0, 0)
                            : new AdminDailyRevenueResponse(
                                    date, row.getRevenue(), row.getPaymentCount(), row.getPaidOrderCount());
                })
                .toList();
    }

    private DateRange resolveRange(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate end = to == null ? today : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        if (start.isAfter(end)) {
            throw new BadRequestException("from must be earlier than or equal to to");
        }
        return new DateRange(start, end);
    }

    private record DateRange(LocalDate from, LocalDate to) {}
}