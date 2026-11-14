package elin.le.giftly.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String itemName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {}
