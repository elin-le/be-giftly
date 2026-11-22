package elin.le.giftly.user.service;

import elin.le.giftly.auth.repository.UserRoleRepository;
import elin.le.giftly.common.mapper.UserMapper;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.user.dto.UpdateAdminProfileRequest;
import elin.le.giftly.user.dto.UpdateUserRequest;
import elin.le.giftly.user.dto.UserResponse;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.secret-code}")
    private String secretCode;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getStatus(),
                roles,
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        user = userRepository.save(user);
        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getStatus(),
                roles,
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserResponse updateAdminProfile(UpdateAdminProfileRequest request) {
        // Verify secret code
        if (!secretCode.equals(request.secretCode())) {
            throw new BadRequestException("Invalid secret code");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        user = userRepository.save(user);
        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getStatus(),
                roles,
                user.getCreatedAt()
        );
    }
}
