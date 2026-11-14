package elin.le.giftly.giftbox.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GiftBoxResponse(
        UUID id,
        BoxProductResponse boxProduct,
        ProductResponse wrappingProduct,
        ProductResponse ribbonProduct,
        ProductResponse cardProduct,
        String greetingMessage,
        List<GiftBoxItemResponse> items,
        BigDecimal totalPrice
) {
    public record ProductResponse(
            UUID id,
            String name,
            BigDecimal price,
            String thumbnailUrl
    ) {}

    public record BoxProductResponse(
            UUID id,
            String name,
            BigDecimal price,
            String thumbnailUrl
    ) {}

    public record GiftBoxItemResponse(
            UUID id,
            UUID productId,
            String productName,
            BigDecimal price,
            Integer quantity,
            BigDecimal subtotal
    ) {}
}
