package elin.le.giftly.admin.dto;

import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxComponentRequest;
import elin.le.giftly.giftbox.dto.GiftBoxItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateAdminGiftBoxRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Box variant ID is required")
        UUID boxVariantId,

        @NotNull(message = "Items list is required")
        List<@Valid GiftBoxItemRequest> items,

        List<@Valid GiftBoxComponentRequest> components
) {
    public CreateGiftBoxRequest toGiftBoxRequest() {
        return new CreateGiftBoxRequest(boxVariantId, items, components == null ? List.of() : components);
    }
}