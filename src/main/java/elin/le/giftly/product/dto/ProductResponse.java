package elin.le.giftly.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        String productType,
        String status,
        Boolean isCustomBoxEligible,
        CategoryResponse category,
        List<ImageResponse> images,
        List<VariantResponse> variants,
        Double averageRating,
        Long reviewCount
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

    public record VariantResponse(
            UUID id,
            String sku,
            BigDecimal price,
            Integer stockQuantity,
            String status
    ) {
    }
}
