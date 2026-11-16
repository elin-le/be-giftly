package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment stage for orders with advance payment support")
public enum PaymentStage {
    @Schema(description = "Full payment in one go")
    FULL_PAYMENT,
    @Schema(description = "Advance payment (e.g., 50% deposit for perishable items)")
    ADVANCE_PAYMENT,
    @Schema(description = "Remaining balance payment after advance")
    REMAINING_PAYMENT
}
