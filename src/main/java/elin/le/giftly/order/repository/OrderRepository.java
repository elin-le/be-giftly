package elin.le.giftly.order.repository;

import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
