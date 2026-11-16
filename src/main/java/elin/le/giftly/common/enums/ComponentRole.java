package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Role of a component in a bundle or order item")
public enum ComponentRole {
    @Schema(description = "The box container")
    BOX,
    @Schema(description = "A regular product inside the bundle")
    PRODUCT,
    @Schema(description = "Packaging accessory (filler, ribbon, etc.)")
    ACCESSORY,
    @Schema(description = "Greeting card")
    GREETING_CARD
}
