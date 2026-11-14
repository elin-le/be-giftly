package elin.le.giftly.order.repository;

import elin.le.giftly.order.entity.OrderItemComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemComponentRepository extends JpaRepository<OrderItemComponent, UUID> {
    List<OrderItemComponent> findByOrderItemId(UUID orderItemId);
}
