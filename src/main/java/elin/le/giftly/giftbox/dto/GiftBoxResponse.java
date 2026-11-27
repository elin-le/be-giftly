package elin.le.giftly.giftbox.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import elin.le.giftly.product.dto.ProductResponse;

public record GiftBoxResponse(
        UUID id,
        BoxProductResponse boxProduct,
        List<ComponentResponse> components,
        List<GiftBoxItemResponse> items,
        BigDecimal totalPrice
) {
    public record BoxProductResponse(
            UUID variantId,
            String name,
            String sku,
            BigDecimal price,
            List<ProductResponse.ImageResponse> images
    ) {}

    public record ComponentResponse(
            UUID variantId,
            String componentType,
            String name,
            String sku,
            BigDecimal price,
            List<ProductResponse.ImageResponse> images
    ) {}

    public record GiftBoxItemResponse(
            UUID id,
            UUID variantId,
            String productName,
            String sku,
            BigDecimal price,
            Integer quantity,
            BigDecimal subtotal,
            List<ProductResponse.ImageResponse> images
    ) {}
}
