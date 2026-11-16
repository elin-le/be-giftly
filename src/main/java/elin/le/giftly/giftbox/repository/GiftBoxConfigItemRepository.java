package elin.le.giftly.giftbox.repository;

import elin.le.giftly.giftbox.entity.GiftBoxConfigItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GiftBoxConfigItemRepository extends JpaRepository<GiftBoxConfigItem, UUID> {
    List<GiftBoxConfigItem> findByConfigId(UUID configId);
}
