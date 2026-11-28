package elin.le.giftly.giftbox.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaveGiftBoxItemsRequest(
        @NotNull(message = "Items list is required")
        List<@Valid GiftBoxItemRequest> items
) {}