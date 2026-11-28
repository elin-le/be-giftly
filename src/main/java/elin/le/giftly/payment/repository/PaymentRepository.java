package elin.le.giftly.payment.repository;

import elin.le.giftly.common.enums.PaymentStatus;
import elin.le.giftly.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(UUID orderId);
    Payment findByOrderIdAndPaymentStatus(UUID orderId, PaymentStatus paymentStatus);
    List<Payment> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = elin.le.giftly.common.enums.PaymentStatus.PAID")
    BigDecimal sumPaidAmount();

    @Query("SELECT COUNT(DISTINCT p.order.id) FROM Payment p WHERE p.paymentStatus = elin.le.giftly.common.enums.PaymentStatus.PAID")
    long countDistinctPaidOrders();

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.paymentStatus = elin.le.giftly.common.enums.PaymentStatus.PAID
            AND p.paidAt >= :from AND p.paidAt < :to
            """)
    BigDecimal sumPaidAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(DISTINCT p.order.id) FROM Payment p
            WHERE p.paymentStatus = elin.le.giftly.common.enums.PaymentStatus.PAID
            AND p.paidAt >= :from AND p.paidAt < :to
            """)
    long countDistinctPaidOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT CAST(p.paid_at AS DATE) AS "revenueDate",
                   COALESCE(SUM(p.amount), 0) AS revenue,
                   COUNT(*) AS "paymentCount",
                   COUNT(DISTINCT p.order_id) AS "paidOrderCount"
            FROM payment p
            WHERE p.payment_status = 'PAID'
              AND p.paid_at >= :from AND p.paid_at < :to
            GROUP BY CAST(p.paid_at AS DATE)
            ORDER BY CAST(p.paid_at AS DATE)
            """, nativeQuery = true)
    List<DailyRevenueProjection> findDailyPaidRevenue(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    interface DailyRevenueProjection {
        LocalDate getRevenueDate();
        BigDecimal getRevenue();
        long getPaymentCount();
        long getPaidOrderCount();
    }
}
