package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Return policy for a product variant")
public enum ReturnPolicy {
    @Schema(description = "Product can be returned within the return window")
    RETURNABLE,
    @Schema(description = "Product cannot be returned (e.g., perishable items)")
    NO_RETURN
}
