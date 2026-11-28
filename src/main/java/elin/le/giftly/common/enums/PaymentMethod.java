package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment method", example = "COD")
public enum PaymentMethod {
    COD,
    ZALOPAY
}
