package elin.le.giftly.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        String productType,
        String customComponentType,
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
            String name,
            CategoryImageResponse image
    ) {
    }

    public record CategoryImageResponse(String publicId, String secureUrl) {}

    public record ImageResponse(
            UUID id,
            String imageUrl,
            String publicId,
            String secureUrl,
            Boolean isPrimary,
            Integer displayOrder
    ) {
    }

    public record OptionValueInfo(
            UUID optionValueId,
            String optionName,
            String value,
            String colorHex
    ) {
    }

    public record VariantResponse(
            UUID id,
            String sku,
            BigDecimal price,
            BigDecimal finalPrice,
            Integer stockQuantity,
            Double length,
            Double width,
            Double height,
            String dimensionUnit,
            Double weight,
            String weightUnit,
            String status,
            List<ImageResponse> images,
            List<OptionValueInfo> optionValues
    ) {
    }
}
