package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Gift box configuration review status", example = "PENDING")
public enum GiftBoxReviewStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REVISION_REQUIRED
}
