package elin.le.giftly.product.repository;

import elin.le.giftly.product.entity.BundleOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BundleOptionGroupRepository extends JpaRepository<BundleOptionGroup, UUID> {
    List<BundleOptionGroup> findByBundleVariantId(UUID bundleVariantId);
}
