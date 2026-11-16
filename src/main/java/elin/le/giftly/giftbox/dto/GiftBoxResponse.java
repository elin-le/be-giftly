package elin.le.giftly.giftbox.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GiftBoxResponse(
        UUID id,
        BoxProductResponse boxProduct,
        List<GiftBoxItemResponse> items,
        BigDecimal totalPrice
) {
    public record BoxProductResponse(
            UUID variantId,
            String name,
            BigDecimal price
    ) {}

    public record GiftBoxItemResponse(
            UUID id,
            UUID variantId,
            String productName,
            String sku,
            BigDecimal price,
            Integer quantity,
            BigDecimal subtotal
    ) {}
}
