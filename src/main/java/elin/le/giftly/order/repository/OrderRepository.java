package elin.le.giftly.order.repository;

import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByUserId(UUID userId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByUserIdAndStatus(UUID userId, OrderStatus status, Pageable pageable);

    long countByOrderedAtGreaterThanEqualAndOrderedAtLessThan(LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT COUNT(DISTINCT o.userId) FROM Order o
            WHERE o.orderedAt >= :from AND o.orderedAt < :to
            """)
    long countDistinctCustomersBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE EXISTS (
                SELECT p.id FROM Payment p
                WHERE p.order.id = o.id
                  AND p.paymentStatus = elin.le.giftly.common.enums.PaymentStatus.PAID
                  AND p.paidAt >= :from AND p.paidAt < :to
            )
            """)
    BigDecimal sumPaidOrderTotalsBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE EXISTS (
                SELECT p.id FROM Payment p
                WHERE p.order.id = o.id
                  AND p.paymentStatus = elin.le.giftly.common.enums.PaymentStatus.PAID
            )
            """)
    BigDecimal sumPaidOrderTotals();

    List<Order> findTopByUserId(UUID userId, org.springframework.data.domain.Pageable pageable);

    @Query(value = """
            SELECT o FROM Order o
            WHERE o.userId = :userId AND o.orderedAt < :orderedAt
            ORDER BY o.orderedAt DESC
            """)
    List<Order> findByUserIdAndOrderedAtBefore(
            @Param("userId") UUID userId,
            @Param("orderedAt") LocalDateTime orderedAt,
            @Param("limit") int limit
    );
}
