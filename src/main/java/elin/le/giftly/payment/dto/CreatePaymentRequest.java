package elin.le.giftly.payment.dto;

import elin.le.giftly.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {}
