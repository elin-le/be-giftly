package elin.le.giftly.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productVariantId,
        String productName,
        BigDecimal price,
        String thumbnailUrl,
        Integer quantity
) {}
