package elin.le.giftly.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ImageInfo(
    @NotBlank(message = "Public ID is required")
    String publicId,

    @NotBlank(message = "Secure URL is required")
    String secureUrl
) {}
