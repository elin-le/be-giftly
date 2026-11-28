package elin.le.giftly.giftbox.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateGiftBoxRequest(
        @NotNull(message = "Box variant ID is required")
        UUID boxVariantId,

        @NotNull(message = "Components list is required")
        List<@Valid GiftBoxComponentRequest> components,

        @NotNull(message = "Items list is required")
        List<@Valid GiftBoxItemRequest> items
) {}