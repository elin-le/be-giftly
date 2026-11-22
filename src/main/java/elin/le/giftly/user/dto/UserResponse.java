package elin.le.giftly.user.dto;

import elin.le.giftly.common.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        UserStatus status,
        List<String> roles,
        LocalDateTime createdAt
) {
}
