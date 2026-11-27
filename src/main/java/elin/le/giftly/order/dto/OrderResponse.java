package elin.le.giftly.order.dto;

import elin.le.giftly.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        BigDecimal requiredAdvanceAmount,
        OrderStatus status,
        LocalDateTime orderedAt,
        AddressResponse address,
        List<OrderItemResponse> items
) {
    public record AddressResponse(
            UUID id,
            String recipientName,
            String phone,
            String addressLine,
            String ward,
            String district,
            String city
    ) {}
}
