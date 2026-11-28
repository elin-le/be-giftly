package elin.le.giftly.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImageInfo(
    @NotBlank(message = "Public ID is required")
    @Size(max = 255, message = "Public ID must not exceed 255 characters")
    String publicId,

    @NotBlank(message = "Secure URL is required")
    @Size(max = 500, message = "Secure URL must not exceed 500 characters")
    String secureUrl
) {}
