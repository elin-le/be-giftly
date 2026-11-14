package elin.le.giftly.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone
) {
}
