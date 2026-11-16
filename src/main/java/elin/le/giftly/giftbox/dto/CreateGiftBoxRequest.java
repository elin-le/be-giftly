package elin.le.giftly.giftbox.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateGiftBoxRequest(
        @NotNull(message = "Box variant ID is required")
        UUID boxVariantId,

        @NotNull(message = "At least one item is required")
        List<GiftBoxItemRequest> items
) {}
