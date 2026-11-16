package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Unit for weight measurements")
public enum WeightUnit {
    @Schema(description = "Grams")
    G,
    @Schema(description = "Kilograms")
    KG,
    @Schema(description = "Pounds")
    LB
}
