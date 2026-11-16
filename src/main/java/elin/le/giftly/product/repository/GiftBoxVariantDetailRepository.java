package elin.le.giftly.product.repository;

import elin.le.giftly.product.entity.GiftBoxVariantDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GiftBoxVariantDetailRepository extends JpaRepository<GiftBoxVariantDetail, UUID> {
    GiftBoxVariantDetail findByVariantId(UUID variantId);
}
