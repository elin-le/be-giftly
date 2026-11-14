package elin.le.giftly.giftbox.repository;

import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GiftBoxConfigRepository extends JpaRepository<GiftBoxConfig, UUID> {
    List<GiftBoxConfig> findByUserId(UUID userId);
}
