package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of selection for a bundle option group")
public enum SelectionType {
    @Schema(description = "Customer selects exactly one option")
    SINGLE,
    @Schema(description = "Customer can select multiple options")
    MULTIPLE
}
