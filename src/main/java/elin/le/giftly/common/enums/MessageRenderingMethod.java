package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How the greeting card message is rendered")
public enum MessageRenderingMethod {
    @Schema(description = "Message is handwritten by staff")
    HANDWRITTEN,
    @Schema(description = "Message is printed on the card")
    PRINTED
}
