package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment status", example = "PAID")
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
