package elin.le.giftly.giftbox.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public record CreateGiftBoxRequest(
        @NotNull(message = "Box variant ID is required")
        UUID boxVariantId,

        @NotNull(message = "At least one item is required")
        List<@Valid GiftBoxItemRequest> items,

        List<@Valid GiftBoxComponentRequest> components
) {
    public CreateGiftBoxRequest(UUID boxVariantId, List<GiftBoxItemRequest> items) {
        this(boxVariantId, items, List.of());
    }
}
