package elin.le.giftly.product.repository;

import elin.le.giftly.product.entity.BundleOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BundleOptionRepository extends JpaRepository<BundleOption, UUID> {
    List<BundleOption> findByOptionGroupId(UUID optionGroupId);
}
