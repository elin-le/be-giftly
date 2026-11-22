package elin.le.giftly.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAddressRequest(
        @NotBlank(message = "Recipient name is required")
        String recipientName,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotBlank(message = "Address line is required")
        String addressLine,

        String ward,

        String district,

        @NotBlank(message = "City is required")
        String city,

        Boolean isDefault
) {}
