package elin.le.giftly.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String productType,
        String status,
        CategoryResponse category,
        List<ImageResponse> images
) {
    public record CategoryResponse(
            UUID id,
            String name
    ) {
    }

    public record ImageResponse(
            UUID id,
            String imageUrl,
            String publicId,
            String secureUrl,
            Boolean isThumbnail
    ) {
    }
}
