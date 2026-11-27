package elin.le.giftly.giftbox.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PurchaseGiftBoxRequest(
        @NotNull(message = "Address ID is required")
        UUID addressId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {}