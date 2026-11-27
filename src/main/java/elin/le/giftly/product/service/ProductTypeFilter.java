package elin.le.giftly.product.service;

import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.exception.model.BadRequestException;

import java.text.Normalizer;
import java.util.Locale;

public record ProductTypeFilter(ProductType productType, Boolean customBoxEligible) {

    public static ProductTypeFilter from(String type) {
        if (type == null || type.isBlank()) {
            return new ProductTypeFilter(null, null);
        }

        String normalizedType = normalize(type);
        return switch (normalizedType) {
            case "CUSTOM", "CUSTOM_BOX", "TUY_CHINH" -> new ProductTypeFilter(null, true);
            case "MUA_LE", "RETAIL", "SINGLE", "REGULAR" ->
                    new ProductTypeFilter(ProductType.REGULAR, null);
            case "SET", "GIFT_SET" -> new ProductTypeFilter(ProductType.READY_MADE_GIFT_SET, null);
            case "BOX", "GIFT_BOX", "HOP" -> new ProductTypeFilter(ProductType.READY_MADE_GIFT_BOX, null);
            default -> parseProductType(normalizedType, type);
        };
    }

    private static ProductTypeFilter parseProductType(String normalizedType, String originalType) {
        try {
            return new ProductTypeFilter(ProductType.valueOf(normalizedType), null);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported product type filter: " + originalType);
        }
    }

    private static String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd');
        return withoutAccents.replaceAll("[\\s-]+", "_").toUpperCase(Locale.ROOT);
    }
}