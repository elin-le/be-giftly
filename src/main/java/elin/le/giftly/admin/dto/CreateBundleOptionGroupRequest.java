package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.GroupType;
import elin.le.giftly.common.enums.SelectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateBundleOptionGroupRequest(
    @NotNull(message = "Bundle variant ID is required")
    UUID bundleVariantId,

    @NotBlank(message = "Group name is required")
    String name,

    @NotNull(message = "Group type is required")
    GroupType groupType,

    @NotNull(message = "Selection type is required")
    SelectionType selectionType,

    Integer minSelections,
    Integer maxSelections,
    Boolean isRequired,
    Integer displayOrder,

    List<CreateBundleOptionRequest> options
) {}
