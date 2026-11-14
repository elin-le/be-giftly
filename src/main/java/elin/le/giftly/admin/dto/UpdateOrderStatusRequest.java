package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Order status is required")
    OrderStatus status
) {}
