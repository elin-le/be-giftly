package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category of a bundle option group")
public enum GroupType {
    @Schema(description = "Greeting card selection group")
    GREETING_CARD,
    @Schema(description = "Packaging accessory selection group")
    PACKAGING_ACCESSORY,
    @Schema(description = "Other customizable option group")
    OTHER
}
