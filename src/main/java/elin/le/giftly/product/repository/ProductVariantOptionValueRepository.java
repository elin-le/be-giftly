package elin.le.giftly.product.repository;

import elin.le.giftly.product.entity.ProductVariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantOptionValueRepository extends JpaRepository<ProductVariantOptionValue, UUID> {
    List<ProductVariantOptionValue> findByVariantId(UUID variantId);
    List<ProductVariantOptionValue> findByOptionValueId(UUID optionValueId);

    @Query("SELECT pvo.optionValue.id FROM ProductVariantOptionValue pvo WHERE pvo.variant.id = :variantId")
    List<UUID> findOptionValueIdsByVariantId(@Param("variantId") UUID variantId);
}
