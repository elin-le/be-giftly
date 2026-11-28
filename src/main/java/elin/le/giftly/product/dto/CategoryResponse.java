package elin.le.giftly.product.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        ImageResponse image
) {
    public record ImageResponse(String publicId, String secureUrl) {}
}
