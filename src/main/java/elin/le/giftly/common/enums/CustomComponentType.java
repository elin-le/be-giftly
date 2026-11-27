package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Selectable component type in a custom gift box")
public enum CustomComponentType {
    BOX,
    WRAPPING,
    RIBBON,
    FILLER,
    GREETING_CARD,
    ACCESSORY
}