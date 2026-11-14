package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order status", example = "DELIVERED")
public enum OrderStatus {
    PENDING_CONFIRMATION,
    CONFIRMED,
    PROCESSING,
    PACKAGING,
    READY_TO_SHIP,
    SHIPPING,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    RETURN_REQUESTED,
    RETURNED,
    FAILED
}
