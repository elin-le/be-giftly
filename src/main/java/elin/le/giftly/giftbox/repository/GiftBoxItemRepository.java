package elin.le.giftly.giftbox.repository;

import elin.le.giftly.giftbox.entity.GiftBoxItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GiftBoxItemRepository extends JpaRepository<GiftBoxItem, UUID> {
    List<GiftBoxItem> findByConfigId(UUID configId);
}
