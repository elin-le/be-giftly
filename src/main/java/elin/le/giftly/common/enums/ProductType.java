package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product type", example = "REGULAR")
public enum ProductType {
    @Schema(description = "Regular standalone product (teddy bear, flowers, chocolate, etc.)")
    REGULAR,
    @Schema(description = "Ready-made gift set with pre-configured components")
    READY_MADE_GIFT_SET,
    @Schema(description = "Ready-made gift box sold individually")
    READY_MADE_GIFT_BOX,
    @Schema(description = "Packaging accessory (filler paper, foam, honeycomb paper, etc.)")
    PACKAGING_ACCESSORY,
    @Schema(description = "Greeting card")
    GREETING_CARD
}
