package elin.le.giftly.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAdminProfileRequest(

        @NotBlank(message = "Secret code is required")
        String secretCode,

        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        String password
) {}
