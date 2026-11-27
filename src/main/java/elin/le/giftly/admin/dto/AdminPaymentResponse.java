package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.PaymentMethod;
import elin.le.giftly.common.enums.PaymentStatus;
import elin.le.giftly.common.enums.PaymentStage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Admin payment response includes order user info.
 */
public record AdminPaymentResponse(
        UUID id,
        UUID orderId,
        String orderNumber,
        UUID userId,
        String userEmail,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        PaymentStage paymentStage,
        BigDecimal amount,
        LocalDateTime paidAt
) {}
