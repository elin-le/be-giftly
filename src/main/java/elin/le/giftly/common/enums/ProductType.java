package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product type", example = "REGULAR")
public enum ProductType {
    REGULAR,
    GIFT_BOX,
    WRAPPING,
    RIBBON,
    GREETING_CARD
}
