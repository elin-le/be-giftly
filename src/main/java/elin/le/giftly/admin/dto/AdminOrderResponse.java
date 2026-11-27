package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AdminOrderResponse(
        UUID id,
        String orderNumber,
        UUID userId,
        String userEmail,
        String userFullName,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        BigDecimal requiredAdvanceAmount,
        OrderStatus status,
        LocalDateTime orderedAt,
        AddressResponse address,
        List<AdminOrderItemResponse> items
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

    public record AdminOrderItemResponse(
            UUID id,
            UUID productId,
            String itemName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal subtotal
    ) {}
}
