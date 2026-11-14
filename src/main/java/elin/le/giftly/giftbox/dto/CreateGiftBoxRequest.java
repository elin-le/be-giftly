package elin.le.giftly.giftbox.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateGiftBoxRequest(
        @NotNull(message = "Box product ID is required")
        UUID boxProductId,

        UUID wrappingProductId,

        UUID ribbonProductId,

        UUID cardProductId,

        String greetingMessage,

        @NotNull(message = "At least one item is required")
        List<GiftBoxItemRequest> items
) {}
