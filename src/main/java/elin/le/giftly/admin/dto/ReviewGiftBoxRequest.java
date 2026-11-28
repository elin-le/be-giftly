package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.GiftBoxReviewStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewGiftBoxRequest(
        @NotNull(message = "Review status is required")
        GiftBoxReviewStatus status,

        @Size(max = 2000, message = "Review note must not exceed 2000 characters")
        String note
) {}