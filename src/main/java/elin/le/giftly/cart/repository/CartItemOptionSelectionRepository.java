package elin.le.giftly.cart.repository;

import elin.le.giftly.cart.entity.CartItemOptionSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemOptionSelectionRepository extends JpaRepository<CartItemOptionSelection, UUID> {
    List<CartItemOptionSelection> findByCartItemId(UUID cartItemId);
    void deleteByCartItemId(UUID cartItemId);
}
