package elin.le.giftly.auth.repository;

import elin.le.giftly.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUserId(UUID userId);
    List<UserRole> findByRoleId(UUID roleId);
    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    @Query("SELECT r.name FROM UserRole ur JOIN ur.role r WHERE ur.user.id = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") UUID userId);
}
