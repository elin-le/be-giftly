package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Unit for dimension measurements")
public enum DimensionUnit {
    @Schema(description = "Centimeters")
    CM,
    @Schema(description = "Millimeters")
    MM,
    @Schema(description = "Inches")
    INCH
}
