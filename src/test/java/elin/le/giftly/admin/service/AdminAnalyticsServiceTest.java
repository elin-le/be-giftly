package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.AdminAnalyticsSummaryResponse;
import elin.le.giftly.admin.dto.AdminDailyRevenueResponse;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminAnalyticsServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final AdminAnalyticsService service = new AdminAnalyticsService(paymentRepository, orderRepository);

    @Test
    void calculatesRevenueAverageOrderValueAndCountsForInclusiveDateRange() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 30);
        when(paymentRepository.sumPaidAmountBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
                .thenReturn(new BigDecimal("300.00"));
        when(paymentRepository.countDistinctPaidOrdersBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
                .thenReturn(2L);
        when(orderRepository.sumPaidOrderTotalsBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
                .thenReturn(new BigDecimal("450.00"));
        when(orderRepository.countByOrderedAtGreaterThanEqualAndOrderedAtLessThan(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay())).thenReturn(5L);
        when(orderRepository.countDistinctCustomersBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
                .thenReturn(3L);

        AdminAnalyticsSummaryResponse result = service.getSummary(from, to);

        assertEquals(new BigDecimal("300.00"), result.totalRevenue());
        assertEquals(new BigDecimal("225.00"), result.averageOrderValue());
        assertEquals(2L, result.paidOrderCount());
        assertEquals(5L, result.totalOrderCount());
        assertEquals(3L, result.customerCount());
    }

    @Test
    void fillsMissingDaysInDailyRevenueSeriesWithZeros() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate middle = from.plusDays(1);
        LocalDate to = from.plusDays(2);
        PaymentRepository.DailyRevenueProjection projection = new PaymentRepository.DailyRevenueProjection() {
            @Override
            public LocalDate getRevenueDate() {
                return middle;
            }

            @Override
            public BigDecimal getRevenue() {
                return new BigDecimal("125.50");
            }

            @Override
            public long getPaymentCount() {
                return 1;
            }

            @Override
            public long getPaidOrderCount() {
                return 1;
            }
        };
        when(paymentRepository.findDailyPaidRevenue(from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(projection));

        List<AdminDailyRevenueResponse> result = service.getDailyRevenue(from, to);

        assertEquals(3, result.size());
        assertEquals(BigDecimal.ZERO, result.get(0).revenue());
        assertEquals(new BigDecimal("125.50"), result.get(1).revenue());
        assertEquals(BigDecimal.ZERO, result.get(2).revenue());
        assertEquals(middle, result.get(1).date());
    }

    @Test
    void rejectsInvertedDateRangeBeforeQuerying() {
        assertThrows(BadRequestException.class,
                () -> service.getSummary(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 1)));
        verifyNoInteractions(paymentRepository, orderRepository);
    }
}