package elin.le.giftly.user.repository;

import elin.le.giftly.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserId(UUID userId);
    Address findByUserIdAndIsDefault(UUID userId, Boolean isDefault);
}
