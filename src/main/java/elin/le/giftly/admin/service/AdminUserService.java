package elin.le.giftly.admin.service;

import elin.le.giftly.auth.entity.Role;
import elin.le.giftly.auth.entity.UserRole;
import elin.le.giftly.auth.repository.RoleRepository;
import elin.le.giftly.auth.repository.UserRoleRepository;
import elin.le.giftly.common.enums.UserStatus;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional(readOnly = true)
    public List<UserRole> getUserRoles(UUID userId) {
        return userRoleRepository.findByUserId(userId);
    }

    @Transactional
    public User updateUserStatus(UUID userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    @Transactional
    public UserRole assignRoleToUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        boolean alreadyHasRole = userRoleRepository.existsByUserIdAndRoleId(userId, role.getId());
        if (alreadyHasRole) {
            return userRoleRepository.findByUserId(userId).stream()
                .filter(ur -> ur.getRole().getId().equals(role.getId()))
                .findFirst()
                .orElseThrow();
        }

        UserRole userRole = UserRole.builder()
            .user(user)
            .role(role)
            .build();
        return userRoleRepository.save(userRole);
    }

    @Transactional
    public void removeRoleFromUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        UserRole userRole = userRoleRepository.findByUserId(userId).stream()
            .filter(ur -> ur.getRole().getId().equals(role.getId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("UserRole", "user-role", userId + "-" + roleName));

        userRoleRepository.delete(userRole);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        userRepository.deleteById(userId);
    }
}
