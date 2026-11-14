package elin.le.giftly.admin.config;

import elin.le.giftly.auth.entity.Role;
import elin.le.giftly.auth.entity.UserRole;
import elin.le.giftly.auth.repository.RoleRepository;
import elin.le.giftly.auth.repository.UserRoleRepository;
import elin.le.giftly.common.enums.UserStatus;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Value("${admin.default.full-name}")
    private String adminFullName;

    @Value("${admin.default.phone}")
    private String adminPhone;

    @Override
    @Transactional
    public void run(String... args) {
        // Check if admin already exists
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists: {}", adminEmail);
            return;
        }

        // Create admin user
        User adminUser = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .phone(adminPhone)
                .status(UserStatus.ACTIVE)
                .build();

        adminUser = userRepository.save(adminUser);
        log.info("Admin account created: {}", adminEmail);

        // Assign ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = Role.builder().name("ADMIN").build();
                    return roleRepository.save(newRole);
                });

        UserRole userRole = UserRole.builder()
                .user(adminUser)
                .role(adminRole)
                .build();
        userRoleRepository.save(userRole);

        log.info("ADMIN role assigned to admin account: {}", adminEmail);
    }
}
