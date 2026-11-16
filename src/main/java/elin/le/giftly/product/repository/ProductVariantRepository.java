package elin.le.giftly.product.repository;

import elin.le.giftly.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    List<ProductVariant> findByProductId(UUID productId);
    List<ProductVariant> findByProductIdAndStatus(UUID productId, String status);
    ProductVariant findBySku(String sku);
}
