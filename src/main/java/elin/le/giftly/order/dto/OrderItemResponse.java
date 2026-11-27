package elin.le.giftly.order.dto;

import elin.le.giftly.common.enums.ComponentRole;
import elin.le.giftly.common.enums.MessageRenderingMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productVariantId,
        UUID giftBoxConfigId,
        String itemName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal,
        List<ComponentResponse> components
) {
    public record ComponentResponse(
            UUID productVariantId,
            ComponentRole componentRole,
            String productName,
            String sku,
            Integer quantity,
            BigDecimal unitPrice,
            Boolean isComplimentary,
            String messageText,
            MessageRenderingMethod messageRenderingMethod
    ) {}
}
