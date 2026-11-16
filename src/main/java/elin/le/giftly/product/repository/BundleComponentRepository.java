package elin.le.giftly.product.repository;

import elin.le.giftly.product.entity.BundleComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BundleComponentRepository extends JpaRepository<BundleComponent, UUID> {
    List<BundleComponent> findByBundleVariantId(UUID bundleVariantId);
}
