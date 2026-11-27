package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(
    @NotNull(message = "Payment status is required")
    PaymentStatus status
) {}
