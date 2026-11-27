package elin.le.giftly.giftbox.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GiftBoxComponentRequest(
        @NotNull(message = "Product variant ID is required")
        UUID productVariantId
) {}