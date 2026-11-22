package elin.le.giftly.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record VariantImageRequest(
    @NotBlank(message = "Image URL is required")
    String imageUrl,

    @NotBlank(message = "Secure URL is required")
    String secureUrl,

    @NotBlank(message = "Public ID is required")
    String publicId,

    Boolean isPrimary,
    Integer displayOrder
) {}
