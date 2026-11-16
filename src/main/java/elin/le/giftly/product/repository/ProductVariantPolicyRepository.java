package elin.le.giftly.product.repository;

import elin.le.giftly.product.entity.ProductVariantPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductVariantPolicyRepository extends JpaRepository<ProductVariantPolicy, UUID> {
    ProductVariantPolicy findByVariantId(UUID variantId);
}
