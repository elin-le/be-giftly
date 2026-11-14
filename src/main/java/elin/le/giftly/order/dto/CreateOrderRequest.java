package elin.le.giftly.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Address ID is required")
        UUID addressId
) {}
