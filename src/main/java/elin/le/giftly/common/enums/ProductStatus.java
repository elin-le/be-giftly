package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product status", example = "AVAILABLE")
public enum ProductStatus {
    AVAILABLE,
    OUT_OF_STOCK,
    DISCONTINUED
}
