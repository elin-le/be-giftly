package elin.le.giftly.product.dto;

import elin.le.giftly.common.enums.GroupType;
import elin.le.giftly.common.enums.SelectionType;

import java.util.List;
import java.util.UUID;

public record BundleOptionGroupResponse(
        UUID id,
        String name,
        GroupType groupType,
        SelectionType selectionType,
        Integer minSelections,
        Integer maxSelections,
        Boolean isRequired,
        Integer displayOrder,
        List<BundleOptionResponse> options
) {}
