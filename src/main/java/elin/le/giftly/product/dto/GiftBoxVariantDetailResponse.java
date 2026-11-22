package elin.le.giftly.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GiftBoxVariantDetailResponse(
        UUID id,
        UUID variantId,
        Double innerLength,
        Double innerWidth,
        Double innerHeight,
        BigDecimal maxWeight,
        String dimensionUnit,
        String weightUnit
) {}
