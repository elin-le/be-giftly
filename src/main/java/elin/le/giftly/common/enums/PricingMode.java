package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How the price of a bundle option is determined")
public enum PricingMode {
    @Schema(description = "The option is free (complimentary)")
    FREE,
    @Schema(description = "Use the product variant's own selling price")
    PRODUCT_PRICE,
    @Schema(description = "Use a fixed price defined on the option")
    FIXED_PRICE
}
