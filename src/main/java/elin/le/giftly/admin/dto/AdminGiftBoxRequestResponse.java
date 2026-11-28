package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.GiftBoxReviewStatus;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminGiftBoxRequestResponse(
        UUID id,
        UUID customerId,
        String customerEmail,
        GiftBoxReviewStatus reviewStatus,
        String reviewNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        GiftBoxResponse configuration
) {}