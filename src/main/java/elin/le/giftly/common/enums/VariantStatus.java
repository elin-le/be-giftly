package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product variant status", example = "AVAILABLE")
public enum VariantStatus {
    AVAILABLE,
    OUT_OF_STOCK,
    DISCONTINUED
}
