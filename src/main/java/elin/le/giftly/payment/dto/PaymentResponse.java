package elin.le.giftly.payment.dto;

import elin.le.giftly.common.enums.PaymentMethod;
import elin.le.giftly.common.enums.PaymentStatus;
import elin.le.giftly.common.enums.PaymentStage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        PaymentStage paymentStage,
        BigDecimal amount,
        LocalDateTime paidAt,
        String checkoutUrl
) {}
