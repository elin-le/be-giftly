package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Review status", example = "APPROVED")
public enum ReviewStatus {
    @Schema(description = "Review is pending admin approval")
    PENDING,
    @Schema(description = "Review has been approved by admin")
    APPROVED,
    @Schema(description = "Review has been rejected by admin")
    REJECTED
}
